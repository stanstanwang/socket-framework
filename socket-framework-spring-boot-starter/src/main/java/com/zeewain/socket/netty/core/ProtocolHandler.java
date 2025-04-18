package com.zeewain.socket.netty.core;

import io.netty.channel.Channel;

/**
 * 协议处理器
 *
 * @author stan
 * @date 2023/1/6
 */
public interface ProtocolHandler {

    /**
     * 初始化协议处理器的 pipeline
     */
    void initChannelHandler(Channel ch);

}
