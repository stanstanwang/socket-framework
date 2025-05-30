package com.zeewain.socket.core.handle.negotiation;

import com.zeewain.socket.core.NettyClient;
import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.protocol.NegotiationInfo;
import io.netty.channel.Channel;

import static com.zeewain.socket.protocol.MessageConstants.CLIENT_HELLO;
import static com.zeewain.socket.protocol.MessageConstants.SERVER_HELLO;

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
