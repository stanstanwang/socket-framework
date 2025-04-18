package com.zeewain.socket.core.client;

import cn.hutool.core.thread.NamedThreadFactory;
import com.zeewain.socket.core.client.builder.ConnectionListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 管理 client 的 Channel。
 * 可以做客户端的重连， 并给出正常可用的 channel
 *
 * @author stan
 * @date 2022/7/26
 */
@Slf4j
public class NettyClientManager {

    private final String host;
    private final int port;
    // 额外的属性用于指定 clientHost，可以在压测的时候建立更多链接
    private final String clientHost;
    private final String websocketPath;

    private final boolean reconnect;


    private final NettyClient nettyClient;
    // 加 volatile 主要是避免对象赋值和初始化指令重排
    private volatile Channel defaultChannel;
    private final Collection<ConnectionListener> connectionListeners;

    // 重连相关的操作
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4,
            (new NamedThreadFactory("netty-client-heartbeat", true)));
    private static final long SCHEDULE_DELAY_MILLS = 60 * 1000L;
    private static final long SCHEDULE_INTERVAL_MILLS = 10 * 1000L;
    private ScheduledFuture<?> scheduledFuture;

    public NettyClientManager(String host, int port, boolean reconnect, NettyClient nettyClient,
                              Collection<ConnectionListener> connectionListeners) {
        this(host, port, reconnect, null, null, nettyClient, connectionListeners);
    }

    public NettyClientManager(String host, int port, boolean reconnect, String clientHost, String websocketPath,
                              NettyClient nettyClient, Collection<ConnectionListener> connectionListeners) {
        this.host = host;
        this.port = port;
        this.reconnect = reconnect;
        this.clientHost = clientHost;
        this.websocketPath = websocketPath;
        this.nettyClient = nettyClient;
        this.connectionListeners = connectionListeners != null
                ? connectionListeners : Collections.emptyList();

        // 添加定时器做重连
        if (this.reconnect) {
            this.scheduledFuture = scheduledExecutor.scheduleAtFixedRate(this::reconnectToServer,
                    SCHEDULE_DELAY_MILLS, SCHEDULE_INTERVAL_MILLS, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * 获取默认的channel, 如果当前 channel 没有创建，或者已经销毁，这里会做到重连
     *
     * @return 响应的 channel 必然是可用的 channel
     */
    public Channel getChannel() {
        if (defaultChannel == null || !defaultChannel.isActive()) {
            synchronized (this) {
                // double check
                log.warn("default channel unusable, now try to reconnect...");
                if (defaultChannel == null || !defaultChannel.isActive()) {
                    this.defaultChannel = this.createChannel();
                }
            }
        }
        return defaultChannel;
    }


    /**
     * 关闭当前的连接
     */
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        // 关闭连接
        if (defaultChannel != null) {
            defaultChannel.close();
        }
    }


    // 给定时器调用的方法，确保执行不抛异常，定时器才不会停止
    private void reconnectToServer() {
        try {
            getChannel();
        } catch (Exception e) {
            log.warn(String.format("连接netty[%s:%s]服务端异常", host, port), e);
        }
    }

    /**
     * 获取新的 channel
     */
    private Channel createChannel() {
        // 创建连接
        Channel channel;
        if (websocketPath != null) {
            channel = nettyClient.connectWs(host, port, websocketPath);
        } else {
            channel = nettyClient.connect(host, port, clientHost);
        }

        // 添加连接关闭的监听器
        channel.closeFuture()
                .addListener((ChannelFuture f) -> {
                    for (ConnectionListener l : connectionListeners) {
                        l.disconnected(f.channel());
                    }
                });

        // 连接初始化
        return initChannels(channel);
    }

    private Channel initChannels(Channel channel) {
        for (ConnectionListener l : connectionListeners) {
            l.connected(channel);
        }
        return channel;
    }


}
