package com.zeewain.socket.netty.core.negotiation;

import com.zeewain.socket.netty.client.builder.ConnectionListener;
import com.zeewain.socket.netty.NegotiationInfo;
import io.netty.channel.Channel;
import org.springframework.core.Ordered;

/**
 * @author stan
 * @date 2023/5/5
 */
public class NegotiationListener implements ConnectionListener {

    private final NegotiationClient negotiationClient;

    public NegotiationListener(NegotiationClient negotiationClient) {
        this.negotiationClient = negotiationClient;
    }


    @Override
    public void connected(Channel channel) {
        negotiationClient.clientHello(channel, NegotiationInfo.V2_DEFAULT);
    }

    @Override
    public int getOrder() {
        // 预留一定位置给一些回调需要连接成功后做处理的
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
