package com.zeewain.socket.netty.client.protocol;

import com.zeewain.socket.netty.client.ClientHandlerProvider;
import com.zeewain.socket.netty.core.NettyProperties;
import com.zeewain.socket.netty.core.heartbeat.HeartBeatHandler;
import com.zeewain.socket.netty.exception.ConnectException;
import com.zeewain.socket.netty.Message;
import com.zeewain.socket.netty.ProtocolCodec;
import com.zeewain.socket.netty.rpc.NettyRpcResponseHandler;
import com.zeewain.socket.netty.server.ServerHandlerProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.zeewain.socket.netty.client.NettyClient.WEB_URL_KEY;

/**
 * @author stan
 * @date 2023/1/6
 */
@Slf4j
public class ClientWebsocketProtocolHandler extends ClientSocketProtocolHandler {

    private static final Map<ChannelId,ChannelFuture> handshakeFutureMap = new ConcurrentHashMap<>();


    public ClientWebsocketProtocolHandler(NettyProperties.Client properties, ClientHandlerProvider clientHandlerProvider,
                                          HeartBeatHandler heartBeatHandler, NettyRpcResponseHandler rpcResponseHandler) {
        super(properties, clientHandlerProvider, heartBeatHandler, rpcResponseHandler);
    }

    public ChannelFuture handshakeFuture(Channel channel) {
        return handshakeFutureMap.get(channel.id());
    }


    /**
     * 初始化 websocket 协议处理器
     */
    @Override
    public void initChannelHandler(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        final URI websocketURI = waitUntilURI(pipeline);
        final SslContext sslCtx = getSslContext(websocketURI);

        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), websocketURI.getHost(), websocketURI.getPort()));
        }

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));

        // 处理 websocket 压缩编码的问题
        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);


        // TODO delay stan 2024/9/12 考虑使用 WebSocketClientProtocolHandler，里边会处理掉handshake和ping/pong
        // 处理websocket协议升级
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        pipeline.addLast(new WebsocketProtocolCodec(
                WebSocketClientHandshakerFactory.newHandshaker(
                        websocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders())));
    }


    @SneakyThrows
    private static SslContext getSslContext(URI websocketURI) {
        final boolean ssl = "wss".equalsIgnoreCase(websocketURI.getScheme());
        final SslContext sslCtx;
        if (ssl) {
            // TODO stan 2023/10/23 后续优化下这个处理
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        return sslCtx;
    }

    private static URI waitUntilURI(ChannelPipeline pipeline) {
        URI websocketURI = null;
        while (websocketURI == null) {
            websocketURI = pipeline.channel().attr(WEB_URL_KEY).get();
            log.info("netty client handshake with {}", websocketURI);
            // busy wait 等待外边线程设置好 websocket 的 uri
            if (websocketURI == null) {
                Thread.yield();
            }
        }
        return websocketURI;
    }


    /**
     * 将 websocket 里边的 {@link WebSocketFrame} 转换为 {@link ByteBuf}
     * 并且追加协议处理 {@link ProtocolCodec} 和业务处理器 {@link ServerHandlerProvider}
     */
    // @ChannelHandler.Sharable
    public class WebsocketProtocolCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {

        private final Logger log = LoggerFactory.getLogger(WebsocketProtocolCodec.class);

        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        public WebsocketProtocolCodec(WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            handshakeFuture = ctx.newPromise();
            handshakeFutureMap.put(ctx.channel().id(), handshakeFuture);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            handshakeFutureMap.remove(ctx.channel().id());
        }

        // 连接成功，执行握手请求
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(new ConnectException(String
                        .format("connection closed while websocket handshake: %s", ctx.channel().remoteAddress())));
            }
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.fireExceptionCaught(cause);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel ch = ctx.channel();
            // 处理握手完成
            if (!handshaker.isHandshakeComplete()) {
                try {
                    // 握手完成后，里边会增加对应 websocket 协议处理器
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    log.info("WebSocket Client connected {}!", ch.remoteAddress());
                    ChannelPipeline pipeline = ctx.pipeline();
                    // 将大的 Frame 消息合并成一个给业务使用
                    pipeline.addLast(new WebSocketFrameAggregator(65536));
                    // ws协议升级完成后，加上 socket 的协议处理
                    initSocketHandler(pipeline);
                    handshakeFuture.setSuccess();
                } catch (WebSocketHandshakeException e) {
                    log.info("WebSocket Client failed to connect {}", ch.remoteAddress());
                    handshakeFuture.setFailure(e);
                }
                return;
            }

            // 处理 http 异常
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse (getStatus=" + response.status() +
                                ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
            }

            // 后续走协议转换
            super.channelRead(ctx, msg);
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
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {

            if (frame instanceof PingWebSocketFrame) {
                frame.content().retain();
                ctx.writeAndFlush(new PongWebSocketFrame(frame.content()));
                readIfNeeded(ctx);
                return;
            }
            // 默认 dropPongFrames=true
            if (frame instanceof PongWebSocketFrame /*&& dropPongFrames*/) {
                readIfNeeded(ctx);
                return;
            }

            if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame) {
                out.add(frame.content().retain());
            } else {
                log.warn("unexpected websocket frame {}, close channel", frame);
                ctx.close();
            }
        }
    }

    private static void readIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }



}
