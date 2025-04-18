package com.zeewain.cbb.netty.client;

import com.zeewain.cbb.netty.client.protocol.ClientSocketProtocolHandler;
import com.zeewain.cbb.netty.client.protocol.ClientWebsocketProtocolHandler;
import com.zeewain.cbb.netty.core.NettyProperties;
import com.zeewain.cbb.netty.exception.ConnectException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * netty客户端, 负责生成 Channel， Channel的管理参考 {@link NettyClientManager}
 *
 * @author zwl
 * @author stan
 * @version 2022年04月21日
 **/
@Slf4j
public class NettyClient {

    public static final AttributeKey<URI> WEB_URL_KEY = AttributeKey.valueOf("_WEBSOCKET_URL");

    // 单例对象
    private final NettyProperties.Client properties;
    private final ClientSocketProtocolHandler clientSocketProtocolHandler;
    private final ClientWebsocketProtocolHandler clientWebsocketProtocolHandler;

    // 多例对象
    private final Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup group; // 当前所有客户端线程共用的

    public NettyClient(NettyProperties.Client properties,
                       ClientSocketProtocolHandler clientSocketProtocolHandler,
                       ClientWebsocketProtocolHandler clientWebsocketProtocolHandler) {
        this.properties = properties;
        this.clientSocketProtocolHandler = clientSocketProtocolHandler;
        this.clientWebsocketProtocolHandler = clientWebsocketProtocolHandler;
        this.start();
    }


    /**
     * 初始化客户端
     */
    // @PostConstruct
    public void start() {
        group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
        // .option(ChannelOption.SO_KEEPALIVE, true)
        // .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyClientConfig.getConnectTimeoutMillis())
        // .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
        // .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
        ;

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                if (properties.isWebSocketEnable()) {
                    clientWebsocketProtocolHandler.initChannelHandler(ch);
                } else {
                    clientSocketProtocolHandler.initChannelHandler(ch);
                }
            }
        });
    }


    /**
     * 建立连接， 指定 host 和 port
     *
     * @param clientHost 建立连接的本地 ip
     */
    @SneakyThrows
    public Channel connect(String host, Integer port, String clientHost) {
        if (clientHost == null) {
            return doConnect(host, port, null, null);
        }

        // 0 端口表示客户端端口随机
        return doConnect(host, port, new InetSocketAddress(clientHost, 0), null);
    }

    @SneakyThrows
    public Channel connectWs(String host, Integer port, String websocketPath) {
        return doConnect(host, port, null, websocketPath);
    }


    private Channel doConnect(String host, int port, InetSocketAddress localAddress, String websocketPath) throws InterruptedException {
        ChannelFuture future;
        if (localAddress == null) {
            future = this.bootstrap.connect(host, port);
        } else {
            future = this.bootstrap.connect(InetSocketAddress
                    .createUnresolved(host, port), localAddress);
        }
        if (properties.isWebSocketEnable()) {
            String path = Optional.ofNullable(websocketPath).orElse(properties.getPath());
            URI uri = toURI(properties.getSchema(), host, port, path);
            future.channel().attr(WEB_URL_KEY).set(uri);
            log.info("setting netty client handshake with {}", uri);
        }

        future.await(properties.getConnectTimeoutSecond(), TimeUnit.SECONDS);
        String hostAndPort = String.format("%s:%s", host, port);
        if (future.isCancelled()) {
            throw new ConnectException(String
                    .format("connect cancelled, can not connect to remote server [%s].", hostAndPort));
        } else if (!future.isSuccess()) {
            throw new ConnectException(String
                    .format("connect failed, can not connect to remote server [%s].", hostAndPort), future.cause());
        }

        Channel channel = future.channel();
        // websocket的话，需要等待握手完成才能使用
        if (properties.isWebSocketEnable()) {
            clientWebsocketProtocolHandler.handshakeFuture(channel).sync();
        }

        channel.closeFuture()
                .addListener(f -> log.info("client channel close!!"));
        return channel;
    }


    /**
     * 关闭客户端
     */
    @PreDestroy
    public void stop() {
        log.info("关闭netty客户端");
        if (group != null) {
            // 关闭线程组
            group.shutdownGracefully();
        }
    }


    @SneakyThrows
    private URI toURI(String schema, String host, int port, String path) {
        String url = String.format("%s://%s:%s%s", schema, host, port, path);
        return new URI(url);
    }

}
