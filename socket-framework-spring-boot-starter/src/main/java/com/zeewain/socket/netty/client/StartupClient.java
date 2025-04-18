package com.zeewain.socket.netty.client;

import com.zeewain.socket.netty.client.builder.NettyClientBuilder;
import com.zeewain.socket.netty.core.NettyProperties;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

/**
 * 用于只有一个客户端需要长连接的情况
 *
 * @author stan
 * @date 2023/6/5
 */
@Slf4j
public class StartupClient {

    private final NettyProperties.Client properties;

    private final NettyClientBuilder nettyClientBuilder;

    private volatile NettyClientManager client;

    // TODO delay stan 2024/9/12 这个应该是废弃的，客户端应该可以建立多个链接
    public StartupClient(NettyProperties.Client properties, NettyClientBuilder nettyClientBuilder) {
        this.properties = properties;
        this.nettyClientBuilder = nettyClientBuilder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        try {
            initConnections();
        } catch (Exception e) {
            // 连接不上，不影响服务端启动
            log.warn("连接netty服务端异常", e);
        }
    }

    private void initConnections() {
        if (!properties.isEnable() || !properties.isStartup()) {
            return;
        }

        String host = properties.getHost();
        int port = properties.getPort();
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = nettyClientBuilder.build(host, port);
                }
            }
        }
    }

    public Channel getChannel() {
        initConnections();
        if (client != null) {
            return client.getChannel();
        }
        return null;
    }


    @PreDestroy
    public void close() {
        if (client != null) {
            client.close();
        }
    }


}
