package com.zeewain.cbb.netty;

import com.zeewain.cbb.netty.client.ClientHandlerProvider;
import com.zeewain.cbb.netty.client.DefaultClientHandler;
import com.zeewain.cbb.netty.client.NettyClient;
import com.zeewain.cbb.netty.client.StartupClient;
import com.zeewain.cbb.netty.client.builder.ConnectionListener;
import com.zeewain.cbb.netty.client.builder.NettyClientBuilder;
import com.zeewain.cbb.netty.client.protocol.ClientSocketProtocolHandler;
import com.zeewain.cbb.netty.client.protocol.ClientWebsocketProtocolHandler;
import com.zeewain.cbb.netty.core.NettyProperties;
import com.zeewain.cbb.netty.core.heartbeat.HeartBeatHandler;
import com.zeewain.cbb.netty.core.negotiation.NegotiationClient;
import com.zeewain.cbb.netty.core.negotiation.NegotiationListener;
import com.zeewain.cbb.netty.protocol.IdGenerator;
import com.zeewain.cbb.netty.rpc.NettyRpcResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

/**
 * @author stan
 * @date 2022/8/15
 */

@Configuration
@Import(NettyCoreConfig.class)
@ConditionalOnProperty(prefix = "zeewain.netty.client", name = "enable")
public class NettyClientAutoConfig {


    // 加名字是为了避免同实例的重名
    @Bean("clientSocketProtocolHandler")
    public ClientSocketProtocolHandler clientSocketProtocolHandler(NettyProperties properties,
                                                                   ClientHandlerProvider clientHandlerProvider,
                                                                   HeartBeatHandler heartBeatHandler,
                                                                   NettyRpcResponseHandler rpcResponseHandler) {
        return new ClientSocketProtocolHandler(properties.getClient(), clientHandlerProvider, heartBeatHandler, rpcResponseHandler);
    }

    // 加名字是为了避免同实例的重名
    @Bean("clientWebsocketProtocolHandler")
    public ClientWebsocketProtocolHandler clientWebsocketProtocolHandler(NettyProperties properties,
                                                                         ClientHandlerProvider clientHandlerProvider,
                                                                         HeartBeatHandler heartBeatHandler,
                                                                         NettyRpcResponseHandler rpcResponseHandler) {
        return new ClientWebsocketProtocolHandler(properties.getClient(), clientHandlerProvider, heartBeatHandler, rpcResponseHandler);
    }

    @Bean
    public NettyClient nettyClient(NettyProperties properties,
                                   @Qualifier("clientSocketProtocolHandler") ClientSocketProtocolHandler clientSocketProtocolHandler,
                                   @Qualifier("clientWebsocketProtocolHandler") ClientWebsocketProtocolHandler clientWebsocketProtocolHandler) {
        return new NettyClient(properties.getClient(), clientSocketProtocolHandler, clientWebsocketProtocolHandler);
    }

    @Bean
    public NettyClientBuilder nettyClientBuilder(NettyProperties nettyProperties,
                                                 NettyClient nettyClient,
                                                 Collection<ConnectionListener> connectionListeners) {
        return new NettyClientBuilder(nettyProperties, nettyClient, connectionListeners);
    }

    @Bean
    public StartupClient startupClient(NettyProperties properties, NettyClientBuilder nettyClientBuilder) {
        return new StartupClient(properties.getClient(), nettyClientBuilder);
    }

    @Bean
    public NegotiationListener negotiationListener(NegotiationClient client) {
        return new NegotiationListener(client);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientHandlerProvider defaultClientHandler(IdGenerator idGenerator) {
        return new DefaultClientHandler(idGenerator);
    }

}
