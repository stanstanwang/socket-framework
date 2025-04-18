package com.zeewain.socket.core.server;

import com.zeewain.socket.core.Processor;
import com.zeewain.socket.core.mvc.NettyProcessorManager;
import com.zeewain.socket.netty.HeartbeatMessage;
import com.zeewain.socket.netty.NettyResponse;

import static com.zeewain.socket.netty.MessageConstants.HEARTBEAT_REQ;
import static com.zeewain.socket.netty.MessageConstants.HEARTBEAT_RESP;

/**
 * 处理心跳请求和响应
 *
 * @author stan
 * @date 2022/7/19
 * @deprecated 心跳得单独处理，不然可能因为业务线程的阻塞处理不了请求
 */
@Deprecated
public class HeartBeatProcessor implements Processor<HeartbeatMessage> {

    public HeartBeatProcessor() {
        NettyProcessorManager.register(getMsgCode(), this);
    }

    @Override
    public String getMsgCode() {
        return HEARTBEAT_REQ;
    }

    @Override
    public NettyResponse<HeartbeatMessage> process(HeartbeatMessage param) {
        return NettyResponse.success(HEARTBEAT_RESP, HeartbeatMessage.PONG);
    }

}
