package com.zeewain.socket.netty.core.heartbeat;

import com.zeewain.socket.netty.core.NettyClient;
import com.zeewain.socket.netty.core.NettyHeader;
import com.zeewain.socket.netty.core.NettyMapping;
import com.zeewain.socket.netty.HeartbeatMessage;
import com.zeewain.socket.netty.NettyResponse;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;

import java.util.Map;

import static com.zeewain.socket.netty.MessageConstants.HEARTBEAT;
import static com.zeewain.socket.netty.MessageConstants.HEARTBEAT_REQ;

/**
 * @author stan
 * @date 2022/8/4
 */
@NettyClient
public interface HeartBeatClient {

    /**
     * 发送同步的心跳请求
     */
    @NettyMapping(HEARTBEAT_REQ)
    NettyResponse<HeartbeatMessage> sendHeartbeat(Channel channel, HeartbeatMessage param);

    /**
     * 发送异步的心跳请求
     */
    @NettyMapping(HEARTBEAT_REQ)
    Promise<NettyResponse<HeartbeatMessage>> sendAsyncHeartbeat(Channel channel, HeartbeatMessage param);

    /**
     * 新版心跳，采用无响应值的方式， 更契合netty在没写或者没读的情况下触发
     */
    @NettyMapping(HEARTBEAT)
    void sendHeartbeatNoResp(Channel channel, HeartbeatMessage param);


    @NettyMapping(HEARTBEAT)
    void sendHeartbeatNoRespWithHeader(Channel channel, @NettyHeader Map<String, String> header, HeartbeatMessage param);


    /**
     * 发送同步心跳请求，可以带上 header 信息
     */
    @NettyMapping(HEARTBEAT_REQ)
    NettyResponse<HeartbeatMessage> sendHeartbeatWithHeader(Channel channel, @NettyHeader Map<String, String> header, HeartbeatMessage param);

}
