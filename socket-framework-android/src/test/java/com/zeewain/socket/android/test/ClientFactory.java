package com.zeewain.socket.android.test;

import com.zeewain.socket.starter.NettyClientAutoConfig;
import com.zeewain.socket.starter.NettyCoreConfig;
import com.zeewain.socket.core.client.ClientHandlerProvider;
import com.zeewain.socket.core.client.NettyClient;
import com.zeewain.socket.core.client.StartupClient;
import com.zeewain.socket.core.client.builder.NettyClientBuilder;
import com.zeewain.socket.core.client.protocol.ClientSocketProtocolHandler;
import com.zeewain.socket.core.client.protocol.ClientWebsocketProtocolHandler;
import com.zeewain.socket.core.NettyProperties;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatClient;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatHandler;
import com.zeewain.socket.core.mvc.NettyProcessorPostProcessor;
import com.zeewain.socket.netty.IdGenerator;
import com.zeewain.socket.netty.codec.json.JsonCodec;
import com.zeewain.socket.netty.compress.GzipCompressor;
import com.zeewain.socket.netty.compress.UnCompressor;
import com.zeewain.socket.core.rpc.NettyClientFactory;
import com.zeewain.socket.core.rpc.NettyRpcResponseHandler;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author stan
 * @date 2023/9/20
 */
public class ClientFactory {

    /**
     * 服务端的 host
     */
    private final static String host = "127.0.0.1";

    /**
     * 服务端的 port
     */
    private final static int port = 9205;

    /**
     * 连接websocket的默认路径
     */
    private final static String path = "/websocket";

    /**
     * 是否开启 debug 日志
     */
    private final static boolean debug = true;



    private static StartupClient startupClient = null;
    private static NettyClientFactory nettyClientFactory = null;
    private static NettyProcessorPostProcessor nettyProcessorPostProcessor = null;
    private final static Map<Class<?>, Object> clientMap = new ConcurrentHashMap<>();

    public static StartupClient get() {
        if (startupClient == null) {
            synchronized (ClientFactory.class) {
                if (startupClient == null) {
                    startupClient = create();
                }
            }
        }
        return startupClient;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSender(Class<T> clazz){
        T client = (T) clientMap.get(clazz);
        if(client == null) {
            client = nettyClientFactory.getClient(clazz);
            clientMap.put(clazz, client);
        }
        return client;
    }

    public static void registerReceiver(Class<?> clazz){
        nettyProcessorPostProcessor._innerRegister(clazz);
    }

    public static void registerReceiver(Object processor){
        nettyProcessorPostProcessor._innerRegister(processor);
    }



    private static StartupClient create() {
        NettyProperties nettyProperties = new NettyProperties();
        nettyProperties.getClient().setHost(host);
        nettyProperties.getClient().setPort(port);
        nettyProperties.getClient().setPath(path);
        nettyProperties.getClient().setDebug(debug);
        nettyProperties.getClient().setEnable(true);

        NettyCoreConfig nettyCoreConfig = new NettyCoreConfig();
        NettyClientAutoConfig nettyClientAutoConfig = new NettyClientAutoConfig();




        // -> -> -> -> core 的配置
        IdGenerator idGenerator = nettyCoreConfig.idGenerator();
        JsonCodec jsonCodec = nettyCoreConfig.jsonCodec();
        GzipCompressor gzipCompressor = nettyCoreConfig.gzipCompressor();
        UnCompressor unCompressor = nettyCoreConfig.unCompressor();

        nettyClientFactory = new NettyClientFactory();
        nettyClientFactory.setNettyProperties(nettyProperties);
        nettyClientFactory.setIdGenerator(idGenerator);


        nettyProcessorPostProcessor = nettyCoreConfig.nettyProcessorPostProcessor();


        HeartBeatClient heartBeatClient = getSender(HeartBeatClient.class);
        HeartBeatHandler heartBeatHandler = nettyCoreConfig
                .heartBeatHandler(heartBeatClient);
        NettyRpcResponseHandler nettyRpcResponseHandler = nettyCoreConfig
                .nettyRpcResponseHandler();

        // -> -> -> -> client 的配置
        ClientHandlerProvider clientHandlerProvider = nettyClientAutoConfig
                .defaultClientHandler(idGenerator);
        ClientSocketProtocolHandler clientSocketProtocolHandler = nettyClientAutoConfig
                .clientSocketProtocolHandler(nettyProperties, clientHandlerProvider, heartBeatHandler, nettyRpcResponseHandler);
        ClientWebsocketProtocolHandler clientWebsocketProtocolHandler = nettyClientAutoConfig
                .clientWebsocketProtocolHandler(nettyProperties, clientHandlerProvider, heartBeatHandler, nettyRpcResponseHandler);


        NettyClient nettyClient = nettyClientAutoConfig
                .nettyClient(nettyProperties, clientSocketProtocolHandler, clientWebsocketProtocolHandler);
        NettyClientBuilder nettyClientBuilder = nettyClientAutoConfig
                .nettyClientBuilder(nettyProperties, nettyClient, Collections.emptyList());

        return nettyClientAutoConfig.startupClient(nettyProperties, nettyClientBuilder);
    }

}
