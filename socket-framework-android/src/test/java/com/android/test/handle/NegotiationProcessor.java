package com.android.test.handle;

import com.zeewain.socket.netty.core.NettyMapping;
import com.zeewain.socket.netty.core.NettyProcessor;
import com.zeewain.socket.netty.core.negotiation.NegotiationClient;
import com.zeewain.socket.netty.NegotiationContext;
import com.zeewain.socket.netty.NegotiationInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import static com.zeewain.socket.netty.MessageConstants.CLIENT_HELLO;
import static com.zeewain.socket.netty.MessageConstants.SERVER_HELLO;

/**
 * @author stan
 * @date 2023/4/19
 */

@NettyProcessor
public class NegotiationProcessor {

    private final NegotiationClient negotiationClient;

    public NegotiationProcessor(NegotiationClient negotiationClient) {
        this.negotiationClient = negotiationClient;
    }

    /**
     * 服务端使用，处理客户端协商的信息
     *
     * @param negotiationInfo 协商的信息
     */
    @NettyMapping(CLIENT_HELLO)
    public void clientHello(ChannelHandlerContext ctx, NegotiationInfo negotiationInfo) {
        Channel channel = ctx.channel();
        channel.attr(NegotiationContext.NEGOTIATION_KEY).set(negotiationInfo);
        negotiationClient.serverHello(channel, NegotiationInfo.V2_DEFAULT);
    }


    /**
     * 客户端使用，处理服务端协商的信息
     */
    @NettyMapping(SERVER_HELLO)
    public void serverHello(ChannelHandlerContext ctx, NegotiationInfo negotiationInfo) {
        Channel channel = ctx.channel();
        channel.attr(NegotiationContext.NEGOTIATION_KEY).set(negotiationInfo);
    }

}
