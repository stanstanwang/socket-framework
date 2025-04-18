package com.zeewain.cbb.netty.server.protocol;

import com.zeewain.cbb.netty.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

/**
 * @author stan
 * @date 2023/1/6
 */
@ChannelHandler.Sharable
public class ProtocolSwitcher extends ChannelInboundHandlerAdapter {

    public static final String HANDLER_NAME = "__protocolSwitcher";


    private final ServerSocketProtocolHandler serverSocketProtocolHandler;
    private final ServerWebsocketProtocolHandler websocketProtocolHandler;

    public ProtocolSwitcher(ServerSocketProtocolHandler serverSocketProtocolHandler, ServerWebsocketProtocolHandler websocketProtocolHandler) {
        this.serverSocketProtocolHandler = serverSocketProtocolHandler;
        this.websocketProtocolHandler = websocketProtocolHandler;
    }


    /**
     * 探测下协议， 然后将消息转下去
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[4];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);

            ChannelPipeline pipeline = ctx.pipeline();

            if (ProtocolConstants.matchMagicCode(bytes)) {
                // 使用 socket 协议
                serverSocketProtocolHandler.initChannelHandler(ctx.channel());
            } else {
                // 使用 websocket 协议
                websocketProtocolHandler.initChannelHandler(ctx.channel());
            }
            // ctx.fireChannelActive();
            pipeline.remove(HANDLER_NAME);
        }
        super.channelRead(ctx, msg);
    }
}
