package com.zeewain.socket.core.handle.heartbeat;

import com.zeewain.socket.core.mvc.AbstractNettyRemoting;
import com.zeewain.socket.protocol.HeartbeatMessage;
import com.zeewain.socket.protocol.Message;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

import static com.zeewain.socket.protocol.MessageConstants.*;

/**
 * netty 服务器端心跳处理器
 *
 * @author zwl
 * @author stan
 * @version 2022年06月28日
 **/
@Slf4j
@ChannelHandler.Sharable
public class HeartBeatHandler extends SimpleChannelInboundHandler<Message> {

    private final HeartBeatClient heartBeatClient;

    public HeartBeatHandler(HeartBeatClient heartBeatClient) {
        this.heartBeatClient = heartBeatClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message req) {
        String msgCode = req.getMsgCode();

        // 双向互发心跳的， 这个时候不用给应答， 是在我们发现很久没有写消息之后才给心跳
        if (HEARTBEAT.equals(msgCode)) {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            log.debug("处理心跳请求 客户端 ip={}, msgId={}, msgCode={}",
                    remoteAddress, req.getId(), msgCode);
        // 这种是客户端发送心跳过来的，  需要给应答
        } else if (HEARTBEAT_REQ.equals(msgCode)) {
            handleHeartbeat(ctx, req);
        } else {
            // 自己不做处理，需要保证计数
            ReferenceCountUtil.retain(req);
            ctx.fireChannelRead(req);
        }
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, Message req) {
        String msgCode = req.getMsgCode();

        long start = System.currentTimeMillis();
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        log.debug("处理心跳请求 客户端 ip={}, msgId={}, msgCode={}",
                remoteAddress, req.getId(), msgCode);
        Message result = new Message();
        result.setId(req.getId());
        // result.setCodec(req.getCodec());
        // result.setCompressor(req.getCompressor());
        result.setRequest(false);
        result.setMsgCode(HEARTBEAT_RESP);
        NettyResponse<HeartbeatMessage> body = NettyResponse.success(HEARTBEAT_RESP, HeartbeatMessage.PONG);
        result.setBody(body);
        AbstractNettyRemoting.writeToChannel(ctx.channel(), result);
        log.debug("处理心跳应答 客户端 ip={}, msgId={}, msgCode={}, respMsgCode={} elapseTime={}ms",
                remoteAddress, req.getId(), msgCode, result.getMsgCode(),
                System.currentTimeMillis() - start);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        IdleStateEvent event = (IdleStateEvent) evt;
        Channel channel = ctx.channel();
        SocketAddress localAddress = channel.localAddress();
        SocketAddress remoteAddress = channel.remoteAddress();

        // 客户端很久没有给消息给服务端了，关闭通道
        if (event.state() == IdleState.READER_IDLE) {
            String channelId = channel.id().asShortText();
            log.warn("连接 id={}, src={} dest={} 已经太久没读到数据了, 关闭通道",
                    channelId, localAddress, remoteAddress);
            channel.close();
        }

        // 服务端已经很久没有给客户端数据了，发送一个心跳包
        else if (event.state() == IdleState.WRITER_IDLE) {
            log.debug("send heartbeat src={} dest={} ", localAddress, remoteAddress);
            heartBeatClient.sendHeartbeatNoResp(channel, HeartbeatMessage.PING);
        }
    }


}
