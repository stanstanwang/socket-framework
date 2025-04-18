package com.zeewain.cbb.netty.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * netty 通用的配置属性
 *
 * @author zwl
 * @author stan
 * @version 2022年02月22日
 **/
@Data
@ConfigurationProperties(prefix = "zeewain.netty")
public class NettyProperties {

    // 一些临时未验证的属性

    public static int serverSocketSendBufSize = Integer.parseInt(System.getProperty(
            "zeewain.netty.transport.serverSocketSendBufSize", String.valueOf(153600)));
    public static int serverSocketResvBufSize = Integer.parseInt(System.getProperty(
            "zeewain.netty.transport.serverSocketResvBufSize", String.valueOf(153600)));
    public static int writeBufferHighWaterMark = Integer.parseInt(System.getProperty(
            "zeewain.netty.transport.writeBufferHighWaterMark", String.valueOf(67108864)));
    public static int writeBufferLowWaterMark = Integer.parseInt(System.getProperty(
            "zeewain.netty.transport.writeBufferLowWaterMark", String.valueOf(1048576)));
    public static int explicitFlushAfterFlushes = Integer.parseInt(System.getProperty(
            "zeewain.netty.transport.explicitFlushAfterFlushes", String.valueOf(256)));

    // 一些临时未验证的属性




    // 过期时间
    private static final int LEASE_EXPIRATION_DURATION_SECONDS = 90;
    // 心跳的续租间隔
    private static final int LEASE_RENEWAL_INTERVAL_SECONDS = 30;

    /**
     * rpc 超时的时间，单位为秒
     */
    private int rpcTimeout = 5;

    /**
     * server 相关的配置
     */
    private Server server = new Server();

    /**
     * client 相关的配置
     */
    private Client client = new Client();

    @Data
    public static class Server {
        /**
         * 是否启动服务端
         */
        private boolean enable = true;

        /**
         * 是否采用 epoll
         */
        private boolean epollEnable = false;

        /**
         * 要绑定到哪个ip，默认选一张可用的ip
         */
        private String host;

        /**
         * 服务端绑定的端口
         */
        private int port = 9205;

        /**
         * 作为 websocket 服务端的时候默认路径
         */
        private String path = "/websocket";

        /**
         * 是否开启 debug 日志
         */
        private boolean debug = false;


        /**
         * 是否开启心跳检查， 不开启心跳检查不会发送心跳，也不会剔除别人
         */
        private boolean healthCheck = true;

        /**
         * 多久进行一次心跳检查， 单位秒，默认90
         */
        private int healthCheckSecond = LEASE_EXPIRATION_DURATION_SECONDS;

        /**
         * 多久发送一次心跳， 单位秒，默认30
         */
        private int heartBeatSecond = LEASE_RENEWAL_INTERVAL_SECONDS;


        /**
         * 半连接队列的大小
         */
        private int backlog = 0;
    }


    /**
     * 一个进程可能只有一个服务端，但是可能会有多个客户端。
     * 这个应该尽量用程序控制，留全局配置会比较麻烦。
     */
    @Data
    public static class Client {
        /**
         * 因为java通常都作为服务端，所以默认客户端不开启
         */
        private boolean enable = false;

        /**
         * 是否跟随服务启动
         */
        private boolean startup = true;

        /**
         * 当前客户端是否使用websocket协议， 使用的话，还需单独配置一下 path
         */
        private boolean webSocketEnable = false;

        /**
         * 服务端的 host
         */
        private String host = "127.0.0.1";

        /**
         * 服务端的 port
         */
        private int port = 9205;

        /**
         * 连接websocket的默认路径
         */
        private String path = "/websocket";


        /**
         * websocket连接时候使用的 schema, 默认为 ws, 需要的时候可以指定为 wss
         */
        private String schema = "ws";

        /**
         * 连接多久超时
         */
        private int connectTimeoutSecond = 10;

        /**
         * 是否开启 debug 日志
         */
        private boolean debug = false;

        /**
         * 用于控制链接断开的时候，是否进行重连
         */
        private boolean reconnect = true;

        /**
         * 是否定时心跳检查， 检查失败的话，会自动进行重连
         */
        private boolean healthCheck = true;

        /**
         * 多久进行一次心跳检查， 单位秒，默认90
         */
        private int healthCheckSecond = LEASE_EXPIRATION_DURATION_SECONDS;

        /**
         * 多久发送一次心跳， 单位秒，默认30
         */
        private int heartBeatSecond = LEASE_RENEWAL_INTERVAL_SECONDS;
    }


}
