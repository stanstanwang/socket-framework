package com.zeewain.socket.core.client.protocol;

import com.zeewain.socket.core.NettyProperties;
import com.zeewain.socket.core.ProtocolHandler;
import com.zeewain.socket.core.rpc.NettyRpcResponseHandler;
import com.zeewain.socket.core.client.ClientHandlerProvider;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatHandler;
import com.zeewain.socket.netty.ProtocolCodec;
import com.zeewain.socket.netty.ProtocolFrameDecoder;
import io.netty.channel.*;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import javax.annotation.PreDestroy;

/**
 * @author stan
 * @date 2023/1/6
 */
public class ClientSocketProtocolHandler implements ProtocolHandler {

    protected final NettyProperties.Client properties;
    private final ChannelInboundHandler clientHandler;
    private final HeartBeatHandler heartBeatHandler;
    private final NettyRpcResponseHandler rpcResponseHandler;

    // 业务处理的线程池
    protected final EventLoopGroup handlerWorkerGroup;


    public ClientSocketProtocolHandler(NettyProperties.Client properties,
                                       ClientHandlerProvider clientHandlerProvider,
                                       HeartBeatHandler heartBeatHandler,
                                       NettyRpcResponseHandler rpcResponseHandler) {
        this.properties = properties;
        this.clientHandler = clientHandlerProvider.get();
        this.heartBeatHandler = heartBeatHandler;
        this.rpcResponseHandler = rpcResponseHandler;
        this.handlerWorkerGroup = new DefaultEventLoopGroup(new DefaultThreadFactory("clientHandler"));
    }


    /**
     * 初始化 socket 协议处理器
     */
    @Override
    public void initChannelHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        initSocketHandler(pipeline);
    }

    /**
     * 通用的方法，统一加上 socket 的处理类，可以给 socket 服务端 和 websocket 服务端使用
     */
    protected void initSocketHandler(ChannelPipeline pipeline) {
        // 处理粘包拆包
        pipeline.addLast(new ProtocolFrameDecoder());
        // 打印日志
        if (properties.isDebug()) {
            pipeline.addLast(new LoggingHandler());
        }
        pipeline.addLast(new FlushConsolidationHandler(NettyProperties.explicitFlushAfterFlushes, true));

        // 处理协议的编解码
        pipeline.addLast(new ProtocolCodec());

        // 加上心跳检查
        if (properties.isHealthCheck()) {
            pipeline.addLast(new IdleStateHandler(
                    // 要比发送心跳时间长一些，不然两个事件会一起触发
                    properties.getHealthCheckSecond(),
                    // 多久没写数据则发送心跳
                    properties.getHeartBeatSecond(),
                    // allIdle 会控制不会触发两次, 但相应也会被其他事件冲掉
                    0));
        }
        pipeline.addLast(heartBeatHandler);

        // 用于处理 rpc 请求的情况
        pipeline.addLast(rpcResponseHandler);
        // 客户端处理类
        if (clientHandler != null) {
            pipeline.addLast(handlerWorkerGroup, clientHandler);
        }
    }


    @PreDestroy
    private void shutDown() {
        handlerWorkerGroup.shutdownGracefully();
    }

}
