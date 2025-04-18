package com.zeewain.cbb.netty.mvc;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.StrUtil;
import com.zeewain.cbb.netty.core.Processor;
import com.zeewain.cbb.netty.protocol.IdGenerator;
import com.zeewain.cbb.netty.protocol.Message;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import com.zeewain.cbb.netty.protocol.base.BaseException;
import com.zeewain.cbb.netty.protocol.base.StatusCodeEnum;
import com.zeewain.cbb.netty.protocol.util.JsonLogUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutorService;

/**
 * 通用的处理消息逻辑，可以给服务端和客户端的 handler 去使用
 *
 * @author stan
 * @date 2022/8/2
 */
@Slf4j
public abstract class AbstractNettyRemoting {

    protected final IdGenerator idGenerator;

    protected AbstractNettyRemoting(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }


    static final boolean reactorPresent;

    static {
        ClassLoader classLoader = AbstractNettyRemoting.class.getClassLoader();
        reactorPresent = ClassLoaderUtil.isPresent("reactor.core.publisher.Flux", classLoader);
    }

    /**
     * 写消息的通用方法
     *
     * @param channel 对应的 channel
     * @param message 对应的消息
     */
    public static void writeToChannel(Channel channel, Message message) {
        writeToChannel(channel, message, true);
    }

    public static void writeToChannel(Channel channel, Message message, boolean flush) {
        if (!channel.isActive()) {
            log.error("连接已关闭，忽略消息发送 {}, msgId={}, msgCode={}",
                    channel.remoteAddress(), message.getId(), message.getMsgCode());
            return;
        }

        ChannelFuture channelFuture;
        if (flush) {
            channelFuture = channel.writeAndFlush(message);
        } else {
            channelFuture = channel.write(message);
        }

        channelFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                // 连接已关闭了，忽略就好 io.netty.channel.StacklessClosedChannelException: null
                if (future.cause() instanceof ClosedChannelException) {
                    return;
                }
                log.warn("发送消息异常, 关闭连接", future.cause());
                future.channel().close();
            }
        });
    }


    /**
     * 新的协议处理消息
     */
    protected void handleMsg(ChannelHandlerContext ctx, Message req) {
        if (log.isDebugEnabled()) {
            log.debug("处理请求 客户端 ip={}, msgId={}, msgCode={}, body={}",
                    ctx.channel().remoteAddress(), req.getId(), req.getMsgCode(),
                    JsonLogUtil.toJsonString(req.getBody()));
        }

        // 1. 查找 handler
        String msgCode = req.getMsgCode();
        Pair<Processor<?>, ExecutorService> pair = NettyProcessorManager.find(msgCode);
        if (pair == null) {
            log.warn("错误请求， 客户端 ip={}, msgId={} msgCode={} 找不到对应的处理器",
                    ctx.channel().remoteAddress(), req.getId(), req.getMsgCode());
            return;
        }

        Processor<?> processor = pair.getKey();

        // 2. 执行
        if (pair.getValue() == null) {
            executeAndHandleResult(ctx, req, processor);
        } else {
            pair.getValue().execute(() -> executeAndHandleResult(ctx, req, processor));
        }
    }


    /**
     * 执行请求和处理结果
     *
     * @param req       请求的id
     * @param processor 对应处理器
     */
    @SuppressWarnings({"rawtypes"})
    private void executeAndHandleResult(ChannelHandlerContext ctx, Message req,
                                        Processor processor) {

        long start = System.currentTimeMillis();

        /*
         * 处理请求
         */
        Message result = executeProcessor(ctx, req, processor);

        /*
         * 处理响应： 有结果的时候将结果发送出去
         * 怎么算有结果？ 定义上有响应， 且响应上有数据
         */
        if (processor.hasResponse() && result.getMsgCode() != null) {
            result.setId(req.getId() != null ? req.getId() : idGenerator.next());
            result.setRequest(false);

            if (log.isDebugEnabled()) {
                log.debug("处理请求resp 客户端 ip={}, msgId={}, msgCode={}, reqMsgCode={}, elapseTime={}ms, response={}",
                        ctx.channel().remoteAddress(), req.getId(), result.getMsgCode(), req.getMsgCode(),
                        System.currentTimeMillis() - start,
                        JsonLogUtil.toJsonString(result.getBody())
                );
            }
            handleResult(ctx.channel(), result);
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleResult(Channel channel, Message result) {
        if (reactorPresent) {
            // 处理 reactor 的情况
            if (result.getBody() instanceof Flux) {
                // 暂未实现
                throw new UnsupportedOperationException();
            }

            // 处理 mono 的响应值， 等待有结果异步输出到 channel
            else if (result.getBody() instanceof Mono) {
                Mono<Object> mono = (Mono<Object>) result.getBody();
                // 异步处理，将 mono 的 result 拿到之后再响应给客户端
                // 如果 Mono 执行错误， 则做统一异常处理
                mono.onErrorResume(e -> Mono.just(handleException(e)))
                        .subscribe(d -> {
                            result.setBody(d);
                            writeToChannel(channel, result);
                        });
                return;
            }
        }
        // 兜底的输出方式
        writeToChannel(channel, result);
    }

    @SuppressWarnings({"rawtypes"})
    private static Message executeProcessor(ChannelHandlerContext ctx, Message req, Processor processor) {
        Message result = new Message();
        // 请求类型是什么，响应类型就是什么
        result.setCodec(req.getCodec());
        String reqMsgCode = req.getMsgCode();
        try {
            Object value = processor.processMessage(ctx, req);
            result.setBody(value);

            if (value instanceof NettyResponse) {
                NettyResponse<?> response = (NettyResponse<?>) value;
                // 需要将 msgCode 转换过来
                result.setMsgCode(toRespCode(response.getMsgCode(), reqMsgCode));
                response.clearUnused();
            } else if (value != null) {
                // 有 value 表示有响应， 需要增加 msgCode
                result.setMsgCode(toRespCode(null, reqMsgCode));
            }
        } catch (Throwable e) {
            log.error(String.format("处理请求异常 客户端 ip=%s, msgId=%s, msgCode=%s ：",
                    ctx.channel().remoteAddress(), req.getId(), reqMsgCode), e);

            // 如果需要异常响应，则将响应填充到 Message 里边
            // 这个 Message 后续会 write 到 Channel 里边
            if (processor.hasResponse()) {
                result.setMsgCode(toRespCode(null, reqMsgCode));
                Object body = handleException(e);
                result.setBody(body);
            }
        }
        return result;
    }


    /**
     * 将请求编码自动替换为响应编码， 规则需要符合 _REQ -> RESP, 否则请求msgCode是什么响应也是什么
     *
     * @param respCode   响应编码，用于兼容，之前存在的话，直接响应
     * @param reqMsgCode 请求编码，按规则去替换
     */
    private static String toRespCode(String respCode, String reqMsgCode) {
        if (StrUtil.isNotEmpty(respCode)) {
            return respCode;
        } else if (reqMsgCode.contains("_REQ")) {
            return reqMsgCode.replace("_REQ", "_RESP");
        } else {
            return reqMsgCode;
        }
    }


    /**
     * 统一的异常处理， 后续可以提供扩展的方式支持自定义异常处理
     * @param e 异常
     */
    private static Object handleException(Throwable e) {
        String message = StrUtil.isNotEmpty(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
        int code = StatusCodeEnum.ERROR.code;
        if (e instanceof BaseException) {
            code = ((BaseException) e).getStatusCode();
        }
        return NettyResponse.error(code, message);
    }

}
