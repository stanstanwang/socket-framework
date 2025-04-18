package com.zeewain.socket.core.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;


public interface ClientHandlerProvider {

    /**
     * 建议响应的 Handler 标记为 {@link ChannelHandler.Sharable}， 这样客户端处理理类单例便可
     */
    ChannelInboundHandler get();


}