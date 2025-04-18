package com.zeewain.cbb.netty.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;

/**
 * 提供服务端的 handler 逻辑
 *
 * @author stan
 * @date 2022/7/29
 */
public interface ServerHandlerProvider {

    /**
     * 建议响应的 Handler 标记为 {@link ChannelHandler.Sharable}， 这样服务端处理类单例便可
     */
    ChannelInboundHandler get();
}
