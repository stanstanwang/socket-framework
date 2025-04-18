package com.zeewain.socket.core.rpc;

import cn.hutool.core.util.TypeUtil;
import com.zeewain.socket.core.ChannelContext;
import com.zeewain.socket.core.mvc.AbstractNettyRemoting;
import com.zeewain.socket.netty.IdGenerator;
import com.zeewain.socket.netty.Message;
import com.zeewain.socket.netty.util.JsonLogUtil;
import com.zeewain.socket.core.util.CollectionUtils;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zeewain.socket.core.rpc.RpcMethod.RPC_METHOD_CACHE;

/**
 * 处理代理的逻辑
 *
 * @author stan
 * @date 2022/7/27
 */
@Slf4j
public class NettyClientExecutor implements InvocationHandler {

    private final IdGenerator idGenerator;

    // 超时时间，单位ms
    private final long rpcTimeoutMills;


    public NettyClientExecutor(IdGenerator idGenerator, long timeoutSeconds) {
        this.idGenerator = idGenerator;
        this.rpcTimeoutMills = Duration.ofSeconds(timeoutSeconds).toMillis();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 忽略一些其他方法的运行
        String methodName = method.getName();
        switch (methodName) {
            case "equals":
                return false;
            case "hashCode":
                return methodName.hashCode();
            case "toString":
                return proxy.getClass() + "@" + methodName.hashCode();
        }
        return execute(method, args);
    }


    private Object execute(Method method, Object[] args) throws Throwable {
        // 1. 将方法调用转换为 消息对象
        int id = idGenerator.next();
        RpcMethod rpcMethod = CollectionUtils.computeIfAbsent(RPC_METHOD_CACHE, method, RpcMethod::fromMethod);
        String msgCode = rpcMethod.getMsgCode();
        Message message = toMessage(id, msgCode, rpcMethod, args);

        // 2. 发送请求
        Channel channel = findChannel(args);
        if (log.isDebugEnabled()) {
            log.debug("rpc发送请求，localIp={}, remoteIp={}, msgId={}, msgCode={}, body={}",
                    channel.localAddress(), channel.remoteAddress(), id, msgCode,
                    JsonLogUtil.toJsonString(message.getBody()));
        }/* else {
            log.info("rpc发送请求，客户端 ip={}, msgId={}, msgCode={}",
                    channel.remoteAddress(), id, msgCode);
        }*/

        // 3. 准备一个空 Promise 对象，得提前放好， 避免响应得比较快找不到map中的对象
        Type returnType = rpcMethod.getReturnType();
        long timeoutMills = findTimeoutMills(args);
        RpcPromise<Object> promise = new RpcPromise<>(id, msgCode, timeoutMills, returnType, channel.eventLoop());
        NettyRpcResponseHandler.put(channel, id, promise);
        // 所有write方法异常的时候都要 close 掉
        AbstractNettyRemoting.writeToChannel(channel, message);

        // 如果不需要响应值，就不用走 promise 去 wait 响应结果了
        if (void.class.equals(returnType) || Void.class.equals(returnType)) {
            NettyRpcResponseHandler.remove(channel, id);
            return null;
        }

        // 如果是异步，直接响应便可，否则当前线程挂起等待结果
        if (rpcMethod.isAsync()) {
            return asyncReturn(promise, rpcMethod.getOriginType());
        }

        try {
            // 4. 当前线程挂起， 等待响应结果
            waitPromise(promise);
            // 5. 解析响应结果
            return extractResult(promise);
        } finally {
            NettyRpcResponseHandler.remove(channel, id);
        }
    }


    /**
     * 异步的响应
     *
     * @param promise    promise
     * @param returnType 响应类型
     * @return
     */
    private /*static*/ Object asyncReturn(RpcPromise<Object> promise, Type returnType) {
        // 异步响应类型的兼容， promise/mono/flux

        Class<?> rawClass = TypeUtil.getClass(returnType);

        // promise 类型
        if (Promise.class.isAssignableFrom(rawClass)) {
            return promise;
        }


        // mono 类型
        if (Mono.class.isAssignableFrom(rawClass)) {
            return Mono.create(sink -> {
                GenericFutureListener<Future<? super Object>> listener = future -> {
                    if (future.isSuccess()) {
                        sink.success(future.get());
                    } else {
                        sink.error(future.cause());
                    }
                };
                promise.addListener(listener);
                sink.onDispose(() -> promise.removeListener(listener));
            })
            // 处理延迟的问题，通过这种方式可以让 Mono 更快得提示超时
            // 处理方式是类似的， 同步的话直接等待， promise的话是定时器扫描
            .timeout(Duration.ofMillis(promise.getTimeoutMills()))
            // 生产消息到新的线程，避免消费成功后阻塞io的读写线程
            .publishOn(Schedulers.boundedElastic())
            // 这一步先不加， 让定时器清理就好了
            // .doOnError(TimeoutException.class, promise::tryFailure)
            ;
        }

        throw new IllegalArgumentException(String
                .format("unsupported async return type %s", rawClass));
    }


    private void waitPromise(RpcPromise<Object> promise) {
        try {
            if (!promise.await(promise.getTimeoutMills(), TimeUnit.MILLISECONDS)) {
                promise.setTimeout();
            }
        } catch (InterruptedException e) {
            promise.setFailure(e);
        }
    }

    private Object extractResult(RpcPromise<Object> promise) throws Throwable {
        if (!promise.isSuccess()) {
            throw promise.cause();
        }
        return promise.getNow();
    }


    private Message toMessage(int id, String msgCode, RpcMethod rpcMethod, Object[] args) {
        Message message = new Message();
        message.setId(id);
        // message.setCodec(ProtocolConstants.CODEC);
        // message.setCompressor(ProtocolConstants.COMPRESSOR);
        message.setMsgCode(msgCode);
        int headerIndex = rpcMethod.getHeaderIndex();
        Object requestBody = findBody(headerIndex, args);
        Map<String, String> header = findHeader(headerIndex, args);
        if (header != null) {
            message.getHeadMap().putAll(header);
        }
        message.setBody(requestBody);
        return message;
    }


    private Object findBody(int headerIndex, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (i == headerIndex) {
                continue;
            }
            Object arg = args[i];
            // 后期改为 ignore 的列表
            if (!(arg instanceof Channel) && !(arg instanceof Duration)) {
                return arg;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> findHeader(int headerIndex, Object[] args) {
        if (headerIndex > 0 && headerIndex < args.length) {
            return (Map<String, String>) args[headerIndex];
        }
        return null;
    }

    // 方法调用的时候，可以手动传入超时时间
    private long findTimeoutMills(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Duration) {
                return ((Duration) arg).toMillis();
            }
        }
        return rpcTimeoutMills;
    }

    private Channel findChannel(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Channel) {
                return (Channel) arg;
            }
        }
        return ChannelContext.get().channel();
    }


}
