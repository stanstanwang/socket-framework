package com.zeewain.socket.netty.core.negotiation;

import com.zeewain.socket.netty.core.NettyClient;
import com.zeewain.socket.netty.core.NettyMapping;
import com.zeewain.socket.netty.NegotiationInfo;
import io.netty.channel.Channel;

import static com.zeewain.socket.netty.MessageConstants.CLIENT_HELLO;
import static com.zeewain.socket.netty.MessageConstants.SERVER_HELLO;

/**
 * @author stan
 * @date 2023/4/19
 */
@NettyClient
public interface NegotiationClient {

    /**
     * 客户端发送的协商协议
     */
    @NettyMapping(CLIENT_HELLO)
    void clientHello(Channel channel, NegotiationInfo negotiationInfo);


    /**
     * 服务端应答的协商协议
     */
    @NettyMapping(SERVER_HELLO)
    void serverHello(Channel channel, NegotiationInfo negotiationInfo);
}
