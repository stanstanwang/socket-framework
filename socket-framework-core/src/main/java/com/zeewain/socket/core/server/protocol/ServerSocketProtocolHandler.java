package com.zeewain.socket.core.server.protocol;

import com.zeewain.socket.core.NettyProperties;
import com.zeewain.socket.core.ProtocolHandler;
import com.zeewain.socket.core.rpc.NettyRpcResponseHandler;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatHandler;
import com.zeewain.socket.protocol.ProtocolCodec;
import com.zeewain.socket.protocol.ProtocolFrameDecoder;
import com.zeewain.socket.core.server.ServerHandlerProvider;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import javax.annotation.PreDestroy;

/**
 * @author stan
 * @date 2023/1/6
 */
public class ServerSocketProtocolHandler implements ProtocolHandler {

    protected final NettyProperties.Server properties;
    private final ServerHandlerProvider serverHandler;
    private final HeartBeatHandler heartBeatHandler;
    private final NettyRpcResponseHandler rpcResponseHandler;

    // 业务处理的线程池
    protected final EventLoopGroup handlerWorkerGroup;

    public ServerSocketProtocolHandler(NettyProperties.Server properties,
                                       ServerHandlerProvider serverHandlerProvider,
                                       HeartBeatHandler heartBeatHandler,
                                       NettyRpcResponseHandler rpcResponseHandler) {
        this.properties = properties;
        this.serverHandler = serverHandlerProvider;
        this.heartBeatHandler = heartBeatHandler;
        this.rpcResponseHandler = rpcResponseHandler;
        this.handlerWorkerGroup = new DefaultEventLoopGroup(new DefaultThreadFactory("serverHandler"));
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
        // 打印数据日志
        if (properties.isDebug()) {
            pipeline.addLast(new LoggingHandler());
        }
        pipeline.addLast(new FlushConsolidationHandler(NettyProperties.explicitFlushAfterFlushes, true));

        // 处理协议的编解码
        pipeline.addLast(new ProtocolCodec());

        // 加上心跳检查
        if (properties.isHealthCheck()) {
            pipeline.addLast(new IdleStateHandler(
                    // 多久没读到数据会将连接关闭掉
                    properties.getHealthCheckSecond(),
                    // 多久没写数据则发送心跳
                    // 正常客户端是30s发送一次心跳请求，服务端会自动给应答的，所以这个得延迟些，避免同时给客户端两个请求
                    properties.getHeartBeatSecond() + 2,
                    // allIdle 会控制不会触发两次, 但相应也会被其他事件冲掉
                    0));
        }
        pipeline.addLast(heartBeatHandler);

        // 用于处理 rpc 请求的情况
        pipeline.addLast(rpcResponseHandler);
        // 服务端的最终处理器， 这里提交到了新的线程上下文中， 主要是为了避免业务处理慢的时候不影响心跳
        // 采用新的线程来跑请求是否会慢？ 对单个请求来说是会慢，但是在新线程里边一个线程可以循环处理多个请求， 结果也还好
        if (serverHandler != null) {
            pipeline.addLast(handlerWorkerGroup, serverHandler.get());
        }
    }


    @PreDestroy
    private void shutDown() {
        handlerWorkerGroup.shutdownGracefully();
    }

}
