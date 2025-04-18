package com.zeewain.socket.netty.server;

import com.zeewain.socket.netty.core.NettyProperties;
import com.zeewain.socket.netty.server.protocol.ProtocolSwitcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.annotation.PreDestroy;

/**
 * netty 服务端
 *
 * @author zwl
 * @author stan
 * @version 2022年02月22日
 **/
@Slf4j
public class NettyServer {

    // 单例对象
    protected final NettyProperties.Server properties;
    private final ProtocolSwitcher protocolSwitcher;


    // 多例对象
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    public NettyServer(NettyProperties.Server properties,
                       ProtocolSwitcher protocolSwitcher) {
        this.properties = properties;
        this.protocolSwitcher = protocolSwitcher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void start() {
        // String listenHost = properties.getHost();
        int listenPort = properties.getPort();
        log.info("启动netty服务端，启动端口{}", listenPort);
        // 定义两个线程组，采用reactor主从模式, bossGroup负责处理连接请求，WorkerGroup负责处理业务请求。
        // 默认是cpu核心数*2个线程去处理
        Class<? extends ServerChannel> channelClass;
        if (properties.isEpollEnable()) {
            channelClass = EpollServerSocketChannel.class;
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
        } else {
            channelClass = NioServerSocketChannel.class;
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        }

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    // .option(ChannelOption.SO_REUSEADDR, true)
                    // .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_SNDBUF, NettyProperties.serverSocketSendBufSize)
                    .childOption(ChannelOption.SO_RCVBUF, NettyProperties.serverSocketResvBufSize)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(NettyProperties.writeBufferLowWaterMark,
                                    NettyProperties.writeBufferHighWaterMark));

                    // 调整连接队列， 避免并发高的时候出现拒绝的情况, 参考 https://blog.csdn.net/weixin_44730681/article/details/113728895
                    if (properties.getBacklog() > 0) {
                        serverBootstrap.option(ChannelOption.SO_BACKLOG, properties.getBacklog());
                    }

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(ProtocolSwitcher.HANDLER_NAME, protocolSwitcher);
                        }
                    });

            Channel channel = serverBootstrap.bind(listenPort).sync().channel();

            // 服务端关闭的时候关闭线程池
            channel.closeFuture()
                    .addListener(f -> this.shutDown());
        } catch (Exception e) {
            log.error("netty服务启动异常", e);
            this.shutDown();
            throw new RuntimeException("Server start failed, the listen port: " + listenPort, e);
        }
    }


    @PreDestroy
    private void shutDown() {
        // 非 springboot 项目没有 ApplicationReadyEvent 时间，所以这里可能抛异常
        log.info("关闭netty服务端");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }


    public NettyProperties.Server getProperties() {
        return properties;
    }
}
