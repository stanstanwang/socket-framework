package com.zeewain.socket.netty.client;

import com.zeewain.socket.netty.mvc.AbstractNettyRemoting;
import com.zeewain.socket.netty.IdGenerator;
import com.zeewain.socket.netty.Message;
import com.zeewain.socket.netty.rpc.NettyRpcResponseHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * 客户端处理类
 *
 * @author zwl
 * @author stan
 */
@Slf4j
public class DefaultClientHandler extends AbstractNettyRemoting implements ClientHandlerProvider {


    public DefaultClientHandler(IdGenerator idGenerator) {
        super(idGenerator);
    }

    @Override
    public ChannelInboundHandler get() {
        return new Handler();
    }

    @ChannelHandler.Sharable
    class Handler extends SimpleChannelInboundHandler<Message> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            SocketAddress ip = ctx.channel().remoteAddress();
            try {
                handleMsg(ctx, msg);
            } catch (Exception e) {
                log.error(String.format("handle msg error, ip %s", ip), e);
            }
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            log.info("[netty client]客户端连接[{}], channel[{}]，地址[{}]", ctx.name(),
                    ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
            // handlerAdded 只是初始化完成， 而不是连接完成， 这里才是连接完成
            // lifecycles.forEach(it -> it.connected(ctx));
            ctx.fireChannelActive();
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            log.info("[netty client]客户端断开[{}], channel[{}]，地址[{}]", ctx.name(),
                    ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
            // lifecycles.forEach(it -> it.disconnected(ctx));
            NettyRpcResponseHandler.remove(ctx.channel());
        }

    }
}
