package com.zeewain.socket.netty.core;

import com.zeewain.socket.core.client.NettyClientManager;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatClient;
import com.zeewain.socket.netty.HeartbeatMessage;
import com.zeewain.socket.netty.NettyResponse;
import io.netty.channel.Channel;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stan
 * @date 2022/8/26
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.server.enable=false",
        "zeewain.netty.server.debug=true",
        "zeewain.netty.client.healthCheck=true",
        "zeewain.netty.client.heartBeatSecond=10",
        "zeewain.netty.client.healthCheckSecond=20",
        "zeewain.netty.server.healthCheck=true",
        "zeewain.netty.server.heartBeatSecond=10",
        "zeewain.netty.server.healthCheckSecond=20",

        // "zeewain.netty.client.host=192.168.0.55",
        // "zeewain.netty.client.port=9405",

        // 测试负载均衡
        // "zeewain.netty.client.host=192.168.0.200",
        // "zeewain.netty.client.port=7000",
        // "zeewain.netty.client.host=36.140.35.127",
        // "zeewain.netty.client.port=6790",

        "zeewain.netty.client.host=192.168.0.24",
        "zeewain.netty.client.port=7000"
})
// @ContextConfiguration(classes = BalancerLifecycle.class)
public class BalancerTest extends BaseTest {

    @Autowired
    private HeartBeatClient heartBeatClient;


    @Test
    public void testHeartbeatForBalancer() {
        NettyClientManager clientManager = clientBuilder.doOnConnect(c -> {
            HeartbeatMessage ping = HeartbeatMessage.PING;
            Map<String, String> header = new HashMap<>();
            // header.put("upstream", "ed87514b-7fe2-4861-a0b8-359f8f6e9b952");
            header.put("upstream", "interaction-service-netty-nacos");
            // header.put("zoneId", "10");
            heartBeatClient.sendHeartbeatNoRespWithHeader(c, header, ping);
        }).build();
        Channel channel = clientManager.getChannel();



        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(channel, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Override
    protected void initMainChannel() {
    }
}
