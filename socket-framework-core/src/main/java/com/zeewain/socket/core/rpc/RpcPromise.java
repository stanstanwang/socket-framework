package com.zeewain.socket.core.rpc;

import com.zeewain.socket.netty.Message;
import com.zeewain.socket.netty.codec.BodyCodec;
import com.zeewain.socket.netty.util.JsonLogUtil;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * rpc promise 主要用来重写父类的get方法，响应的时候获得已经转换的结果
 *
 * @author stan
 * @date 2022/8/9
 */
@Slf4j
public class RpcPromise<V> extends DefaultPromise<V> {

    @Getter
    private final int id;
    @Getter
    private final String msgCode;
    @Getter
    private final Type returnType;

    private final long start = System.currentTimeMillis();


    /**
     * 超时时间
     */
    @Getter
    private final long timeoutMills;

    /**
     * 创建等待的 promise 对象
     *
     * @param id         消息的id
     * @param msgCode    消息的code
     * @param timeoutMills 超时时间， 单毫秒
     * @param returnType 响应的类型
     * @param eventLoop  用来通知listener
     */
    public RpcPromise(int id, String msgCode, long timeoutMills, Type returnType, EventLoop eventLoop) {
        super(eventLoop);
        this.id = id;
        this.msgCode = msgCode;
        this.timeoutMills = timeoutMills;
        this.returnType = returnType;
    }

    /**
     * 重写 getNow 方法，将网络请求自动转换为 rpc 需要的响应请求
     */
    @Override
    public V getNow() {
        return castResult(super.getNow());
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return castResult(super.get());
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return castResult(super.get(timeout, unit));
    }

    /**
     * 解析 promise 的结果
     *
     * @param data 数据类型
     */
    @SuppressWarnings("unchecked")
    private V castResult(Object data) {
        if (data == null) {
            return null;
        }

        // 序列化和反序列化在协议编解码中已经处理了
        Message message = (Message) data;
        Object responseBody = message.getBody();

        // 类型不对的话做转换，对的话直接响应
        if (responseBody != null) {
            Class<?> responseClass = responseBody.getClass();
            if (byte[].class.equals(responseClass)
                    && !isInstance(returnType, responseClass)) {
                responseBody = BodyCodec.getByType(message.getCodec())
                        .decode((byte[]) responseBody, returnType);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("rpc发送请求-响应，msgId={}, msgCode={}, elapseTime={}ms, response={}",
                    id, msgCode,
                    this.getElapse(), JsonLogUtil.toJsonString(responseBody));
        }
        return (V) responseBody;
    }

    private boolean isInstance(Type type, Class<?> clazz) {
        // 后边看看是否需要加上泛型的对比
        return type.equals(clazz);
    }

    /**
     * 当前 promise 是否超时
     */
    public boolean isTimeout() {
        return System.currentTimeMillis() - start > timeoutMills;
    }


    /**
     * 将当前 promise 设置为超时异常
     */
    public void setTimeout() {
        TimeoutException timeoutException = new TimeoutException(String
                .format("requestId=%s, msgCode=%s request timeout %s", this.getId(), this.getMsgCode(), timeoutMills));
        this.setFailure(timeoutException);
    }


    /**
     * 计算响应时间
     */
    public long getElapse() {
        return System.currentTimeMillis() - start;
    }
}
