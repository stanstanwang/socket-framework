package com.zeewain.cbb.netty.server.protocol;

import com.zeewain.cbb.netty.core.NettyProperties;
import com.zeewain.cbb.netty.core.heartbeat.HeartBeatHandler;
import com.zeewain.cbb.netty.protocol.Message;
import com.zeewain.cbb.netty.protocol.ProtocolCodec;
import com.zeewain.cbb.netty.rpc.NettyRpcResponseHandler;
import com.zeewain.cbb.netty.server.ServerConnectionListener;
import com.zeewain.cbb.netty.server.ServerHandlerProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stan
 * @date 2023/1/6
 */
@Slf4j
public class ServerWebsocketProtocolHandler extends ServerSocketProtocolHandler {

    private final WebsocketProtocolCodec websocketProtocolCodec = new WebsocketProtocolCodec();
    private final List<ServerConnectionListener> connectionListeners;

    public ServerWebsocketProtocolHandler(NettyProperties.Server properties, ServerHandlerProvider serverHandlerProvider,
                                          HeartBeatHandler heartBeatHandler, NettyRpcResponseHandler rpcResponseHandler, List<ServerConnectionListener> connectionListeners) {
        super(properties, serverHandlerProvider, heartBeatHandler, rpcResponseHandler);
        this.connectionListeners = connectionListeners.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
    }


    /**
     * 初始化 websocket 协议处理器
     */
    @Override
    public void initChannelHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 首次websocket链接校验下http链接
        pipeline.addLast(getRequestHandler());
        // 处理 websocket 处理压缩编码的问题
        pipeline.addLast(new WebSocketServerCompressionHandler());
        // 处理 websocket 的协议， 还有处理控制的 Ping/Pong/Close 等 frame, 将报文解析为 WebSocketFrame

        String websocketPath = properties.getPath();
        String subprotocols = null;
        boolean allowExtensions = true;
        int maxFrameSize = 1024 * 1024; // 1mb
        boolean allowMaskMismatch = false;
        boolean checkStartsWith = true;
        boolean dropPongFrames = true;
        long handshakeTimeoutMillis = 10000L;

        // TODO delay stan 2024/9/12 websocketPath应该是可以有多个的
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, subprotocols, allowExtensions,
                maxFrameSize, allowMaskMismatch, checkStartsWith, dropPongFrames, handshakeTimeoutMillis));

        // websocket协议升级之后的处理
        pipeline.addLast(websocketProtocolCodec);
    }

    // 保存websocket的 url
    private ChannelInboundHandlerAdapter getRequestHandler() {
        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof HttpRequest) {
                    for (ServerConnectionListener connectionListener : connectionListeners) {
                        HttpResponse httpResponse = connectionListener.onWsConnected(ctx, (HttpRequest) msg);
                        if (httpResponse != null) {
                            log.info("websocket connected resp {}, close the channel", httpResponse.status());
                            ctx.writeAndFlush(httpResponse);
                            ctx.close();
                            return;
                        }
                    }
                }
                super.channelRead(ctx, msg);
            }
        };
    }


    /**
     * 将 websocket 里边的 {@link WebSocketFrame} 转换为 {@link ByteBuf}
     * 并且追加协议处理 {@link ProtocolCodec} 和业务处理器 {@link ServerHandlerProvider}
     */
    @ChannelHandler.Sharable
    public class WebsocketProtocolCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {
        private final Logger log = LoggerFactory.getLogger(WebsocketProtocolCodec.class);

        /**
         * websocket 升级成功之后， 给该 channel 追加子协议处理器
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                String subProtocol = ((WebSocketServerProtocolHandler.HandshakeComplete) evt).selectedSubprotocol();
                log.info("upgraded to websockets remoteIp={}, subProtocol={}",
                        ctx.channel().remoteAddress(), subProtocol);

                ChannelPipeline pipeline = ctx.pipeline();
                // TODO delay stan 2024/9/12, 需要验证下，这个handler应该是放前边的
                pipeline.addLast(new WebSocketFrameAggregator(65536)); // 入栈
                initSocketHandler(pipeline);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }


        /**
         * 出栈，{@link Message } -> {@link ByteBuf} -> {@link WebSocketFrame}
         */
        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
            // 要传递下去， 都得 retain 下
            out.add(new BinaryWebSocketFrame(msg.retain()));
        }


        /**
         * 入栈，{@link WebSocketFrame} -> {@link ByteBuf} -> {@link Message}
         */
        @Override
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> out) {
            if (webSocketFrame instanceof TextWebSocketFrame || webSocketFrame instanceof BinaryWebSocketFrame) {
                out.add(webSocketFrame.content().retain());
            } else {
                ctx.close();
            }
        }
    }


}
