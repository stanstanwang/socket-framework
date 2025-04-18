package com.zeewain.cbb.netty.server;


import com.zeewain.cbb.netty.mvc.AbstractNettyRemoting;
import com.zeewain.cbb.netty.protocol.IdGenerator;
import com.zeewain.cbb.netty.protocol.Message;
import com.zeewain.cbb.netty.rpc.NettyRpcResponseHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.net.SocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 默认的服务端处理器
 */
@Slf4j
public class DefaultServerHandler extends AbstractNettyRemoting implements ServerHandlerProvider {

    private final List<ServerConnectionListener> connectionListeners;

    private volatile Handler handler;


    public DefaultServerHandler(IdGenerator idGenerator, List<ServerConnectionListener> connectionListeners) {
        super(idGenerator);
        this.connectionListeners = connectionListeners.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
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
            log.info("[netty server]客户端连接[{}], channel[{}]，地址[{}]",
                    ctx.name(), ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
            connectionListeners.forEach(it -> it.connected(ctx));
        }

        // handler 后续加载进来的话，这一步就错过了
        /*@Override
        public void channelActive(ChannelHandlerContext ctx) {
            // handlerAdded 只是初始化完成， 而不是连接完成， 这里才是连接完成
            // connectionListeners.forEach(it -> it.connected(ctx));
            ctx.fireChannelActive();
        }*/

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            log.info("[netty server]客户端断开[{}], channel[{}]，地址[{}]",
                    ctx.name(), ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
            connectionListeners.forEach(it -> it.disconnected(ctx));
            NettyRpcResponseHandler.remove(ctx.channel());
        }
    }


    @Override
    public ChannelInboundHandler get() {
        // double check, need volatile to avoid reorder initialization
        if (this.handler == null) {
            synchronized (this) {
                if (this.handler == null) {
                    this.handler = new Handler();
                }
            }
        }
        return this.handler;
    }
}
