package com.zeewain.socket.netty.core;

import com.zeewain.socket.core.handle.heartbeat.HeartBeatClient;
import com.zeewain.socket.protocol.HeartbeatMessage;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.channel.Channel;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        // "zeewain.netty.client.port=9206",

        // 测试负载均衡
        // "zeewain.netty.client.host=36.140.35.127",
        // "zeewain.netty.client.port=6790",
})
public class HeartbeatTest extends BaseTest {

    @Autowired
    private HeartBeatClient heartBeatClient;


    @Test
    public void testHeartbeatWithAssert() {
        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(mainChannel, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testHeartbeatForBalancer() {
        HeartbeatMessage ping = HeartbeatMessage.PING;

        Map<String, String> header = new HashMap<>();
        // header.put("upstream", "ed87514b-7fe2-4861-a0b8-359f8f6e9b952");
        header.put("upstream", "vr-meeting-msg-netty-nacos");
        header.put("meetingId", "10");
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeatWithHeader(mainChannel, header, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testHeartbeatForAuth() {
        HeartbeatMessage ping = HeartbeatMessage.PING;

        Map<String, String> header = new HashMap<>();
        header.put("x-zwtoken", "testappid2.BA3LF8AUwn6bKns_t6CB5nzBxHIAYL-gbeelF7V7HpU=.eyJhcHBJZCI6IjEiLCJleHBpcmVBdCI6MTY3NTQyNjQ2NjI0MywicGVybWlzc2lvbiI6ImFkbWluIiwicm9vbUlkIjoiMSIsInVzZXJJZCI6IjEifQ==");
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeatWithHeader(mainChannel, header, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testConnectionWithLocal() throws Exception {
        Channel aa = clientBuilder.build("127.0.0.1").getChannel();
        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(aa, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testSleep() throws Exception {
        // 测试 sleep
        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

}
