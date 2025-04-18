package com.zeewain.socket.core.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 客户端连接的生命周期, 用于处理客户端的回调
 *
 * @author stan
 * @date 2022/8/16
 */
public interface ServerConnectionListener extends Ordered {


    /**
     * 处理 websocket 链接的问题
     *
     * @param httpRequest http 请求对象，可以解析链接
     * @return 可空，null会将请求往下传递处理，非null会结束当前websocket的请求
     */
    default @Nullable HttpResponse onWsConnected(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        return null;
    }

    /**
     * 工具方法，用于构建 resp
     *
     * @param req     原请求
     * @param status  状态
     * @return http request
     */
    public static HttpResponse constructResp(HttpRequest req, HttpResponseStatus status) {
        return constructResp(req, status, new byte[0]);
    }
    public static HttpResponse constructResp(HttpRequest req, HttpResponseStatus status, byte[] content) {
        FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), status,
                Unpooled.wrappedBuffer(content));
        response.headers()
                .set(CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    /**
     * channel 连接的时候调用,  可以做channel管理，或者重新发送注册信息
     *
     * @param ctx 当前 channel 的上下文信息
     */
    default void connected(ChannelHandlerContext ctx) {
    }


    /**
     * channel 断开连接的时候调用
     *
     * @param ctx 当前 channel 的上下文信息
     */
    default void disconnected(ChannelHandlerContext ctx) {
    }


    /**
     * 当前回调的优先级， 数字越小优先级越高
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
