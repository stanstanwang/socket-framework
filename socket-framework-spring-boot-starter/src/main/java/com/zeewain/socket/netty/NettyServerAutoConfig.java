package com.zeewain.socket.netty;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import com.zeewain.socket.netty.core.NettyProperties;
import com.zeewain.socket.netty.core.heartbeat.HeartBeatHandler;
import com.zeewain.socket.netty.register.NettyAutoRegister;
import com.zeewain.socket.netty.rpc.NettyRpcResponseHandler;
import com.zeewain.socket.netty.server.DefaultServerHandler;
import com.zeewain.socket.netty.server.NettyServer;
import com.zeewain.socket.netty.server.ServerConnectionListener;
import com.zeewain.socket.netty.server.ServerHandlerProvider;
import com.zeewain.socket.netty.server.protocol.ProtocolSwitcher;
import com.zeewain.socket.netty.server.protocol.ServerSocketProtocolHandler;
import com.zeewain.socket.netty.server.protocol.ServerWebsocketProtocolHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * // TODO delay stan 2022/12/29 应该将 spring 和 netty 的分离，这样就可以给 android 直接使用了
 *
 * @author stan
 * @date 2022/7/27
 */
@Configuration
@Import(NettyCoreConfig.class)
@ConditionalOnProperty(prefix = "zeewain.netty.server", name = "enable", matchIfMissing = true)
@AutoConfigureAfter({NacosDiscoveryAutoConfiguration.class, NacosServiceRegistryAutoConfiguration.class})
// @EnableNettyClients 这里定义没用，因为也还需要留在外边定义在哪个 package 引入 client
public class NettyServerAutoConfig {

    // @Bean
    // 不激活，换另外一种方式来处理心跳请求
    /*public HeartBeatProcessor heartBeatProcessor() {
        return new HeartBeatProcessor();
    }*/

    @Bean
    @ConditionalOnMissingBean
    public ServerHandlerProvider serverHandler(IdGenerator idGenerator, List<ServerConnectionListener> connectionListeners) {
        return new DefaultServerHandler(idGenerator, connectionListeners);
    }

    @Bean("serverSocketProtocolHandler")
    public ServerSocketProtocolHandler serverSocketProtocolHandler(NettyProperties properties,
                                                                   ServerHandlerProvider serverHandlerProvider,
                                                                   HeartBeatHandler heartBeatHandler,
                                                                   NettyRpcResponseHandler rpcResponseHandler) {
        return new ServerSocketProtocolHandler(properties.getServer(), serverHandlerProvider, heartBeatHandler, rpcResponseHandler);
    }

    @Bean("serverWebsocketProtocolHandler")
    public ServerWebsocketProtocolHandler serverWebsocketProtocolHandler(NettyProperties properties, ServerHandlerProvider serverHandlerProvider,
                                                                         HeartBeatHandler heartBeatHandler, NettyRpcResponseHandler rpcResponseHandler,
                                                                         List<ServerConnectionListener> connectionListeners
    ) {
        return new ServerWebsocketProtocolHandler(properties.getServer(), serverHandlerProvider, heartBeatHandler, rpcResponseHandler, connectionListeners);
    }

    @Bean
    public ProtocolSwitcher protocolSwitcher(@Qualifier("serverSocketProtocolHandler") ServerSocketProtocolHandler serverSocketProtocolHandler,
                                             @Qualifier("serverWebsocketProtocolHandler") ServerWebsocketProtocolHandler serverWebsocketProtocolHandler) {
        return new ProtocolSwitcher(serverSocketProtocolHandler, serverWebsocketProtocolHandler);
    }

    @Bean
    public NettyServer nettyServer(NettyProperties properties, ProtocolSwitcher protocolSwitcher) {
        return new NettyServer(properties.getServer(), protocolSwitcher);
    }

    @Bean
    @ConditionalOnBean(type = {
            "com.alibaba.cloud.nacos.registry.NacosServiceRegistry",
            "com.alibaba.cloud.nacos.NacosDiscoveryProperties"
    })
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.register-enabled", havingValue = "true", matchIfMissing = true)
    public NettyAutoRegister nettyAutoRegister(NacosServiceRegistry serviceRegistry,
                                               NacosDiscoveryProperties discoveryProperties,
                                               NettyProperties nettyProperties) {
        return new NettyAutoRegister(serviceRegistry, discoveryProperties, nettyProperties.getServer().getPort());
    }


}
