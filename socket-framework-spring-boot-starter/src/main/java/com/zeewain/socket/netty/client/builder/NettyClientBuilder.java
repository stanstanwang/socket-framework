package com.zeewain.socket.netty.client.builder;

import cn.hutool.core.util.URLUtil;
import com.zeewain.socket.netty.client.NettyClient;
import com.zeewain.socket.netty.client.NettyClientManager;
import com.zeewain.socket.netty.core.NettyProperties;
import com.zeewain.socket.netty.util.NetUtil;
import io.netty.channel.Channel;
import org.springframework.core.Ordered;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author stan
 * @date 2023/5/15
 */
public class NettyClientBuilder {

    private final NettyClient nettyClient;
    private final NettyProperties.Client properties;

    // 拦截器的种类
    // 初始化的拦截器
    // 连接成功的拦截
    // 发送请求的拦截器
    private final Collection<ConnectionListener> connectionListeners;
    private final Collection<RequestInterceptor> interceptors;


    public NettyClientBuilder(NettyProperties nettyProperties, NettyClient nettyClient, Collection<ConnectionListener> connectionListeners) {
        this.properties = nettyProperties.getClient();
        this.nettyClient = nettyClient;
        this.connectionListeners = connectionListeners;
        this.interceptors = null;
    }

    private NettyClientBuilder(NettyProperties.Client properties, NettyClient nettyClient,
                               Collection<ConnectionListener> connectionListeners, Collection<RequestInterceptor> interceptors) {
        this.properties = properties;
        this.connectionListeners = connectionListeners;
        this.interceptors = interceptors;
        this.nettyClient = nettyClient;
    }


    /**
     * 连接建立的时候回调
     */
    public NettyClientBuilder doOnConnect(Consumer<Channel> consumer) {
        return addListener(new ConnectionListener() {
            @Override
            public void connected(Channel channel) {
                consumer.accept(channel);
            }
        });
    }


    /**
     * 连接关闭的时候回调
     */
    public NettyClientBuilder doOnClose(Consumer<Channel> consumer) {
        return addListener(new ConnectionListener() {
            @Override
            public void disconnected(Channel channel) {
                consumer.accept(channel);
            }
        });
    }


    public NettyClientBuilder addListener(ConnectionListener... listeners) {
        List<ConnectionListener> additions = Arrays.asList(listeners);
        // 响应新的对象， 保证builder是可重用的
        return new NettyClientBuilder(properties, nettyClient,
                append(connectionListeners, additions), interceptors);
    }


    /**
     * 使用全局配置的默认ip+port去连接
     */
    public NettyClientManager build() {
        return build(properties.getHost(), properties.getPort());
    }

    /**
     * 指定服务端端口，建立连接
     */
    public NettyClientManager build(String host, Integer port) {
        NettyClientManager nettyClientManager = new NettyClientManager(host, port, properties.isReconnect(),
                nettyClient, sort(connectionListeners));
        return doConnect(nettyClientManager);
    }


    // 测试使用
    public NettyClientManager build(String clientHost) {
        NettyClientManager nettyClientManager = new NettyClientManager(
                properties.getHost(), properties.getPort(), properties.isReconnect(), clientHost,
                null, nettyClient, sort(connectionListeners));
        return doConnect(nettyClientManager);
    }


    /**
     * 使用默认路径连接 websocket 服务
     * @return
     */
    public NettyClientManager buildWebsocket() {
        return buildWebsocket(properties.getPath());
    }


    /**
     * 使用指定路径连接 websocket 服务
     *
     * @param path 指定的路径, 也可以指定全路径
     */
    public NettyClientManager buildWebsocket(String path) {
        String host = properties.getHost();
        int port = properties.getPort();

        // 处理完整路径的情况
        if (path.startsWith("ws:") || path.startsWith("wss:")) {
            path = path.replaceFirst("ws:", "http:")
                    .replaceFirst("wss:", "https:");
        }
        if (path.startsWith("http:") || path.startsWith("https:")) {
            URL url = URLUtil.url(path);
            host = url.getHost();
            port = url.getPort();
            if (port == -1) {
                port = path.startsWith("http:") ? 80 : 443;
            }
            path = NetUtil.getPath(path);
        }

        NettyClientManager nettyClientManager = new NettyClientManager(
                host, port, properties.isReconnect(),
                null, path,
                nettyClient, sort(connectionListeners));
        return doConnect(nettyClientManager);
    }


    private static NettyClientManager doConnect(NettyClientManager nettyClientManager) {
        // 初始化化连接
        nettyClientManager.getChannel();
        return nettyClientManager;
    }


    public static <T> Collection<T> append(Collection<? extends T> collection, Collection<? extends T> additions) {
        List<T> result = new ArrayList<>((collection != null) ? collection : Collections.emptyList());
        if (additions != null) {
            result.addAll(additions);
        }
        return Collections.unmodifiableList(result);
    }


    public static <T extends Ordered> Collection<T> sort(Collection<? extends T> collection) {
        return collection.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
    }


}
