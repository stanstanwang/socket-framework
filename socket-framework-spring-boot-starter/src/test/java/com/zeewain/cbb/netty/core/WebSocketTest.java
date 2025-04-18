package com.zeewain.cbb.netty.core;

import com.zeewain.cbb.netty.client.NettyClientManager;
import com.zeewain.cbb.netty.core.heartbeat.HeartBeatClient;
import com.zeewain.cbb.netty.protocol.HeartbeatMessage;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import io.netty.channel.Channel;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author stan
 * @date 2022/8/26
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.server.enable=false",
        "zeewain.netty.server.debug=true",
        "zeewain.netty.client.startup=false",
        "zeewain.netty.client.healthCheck=true",
        "zeewain.netty.client.heartBeatSecond=10",
        "zeewain.netty.client.healthCheckSecond=20",

        "zeewain.netty.client.webSocketEnable=true",

        // direct
        // "zeewain.netty.client.host=127.0.0.1",
        // "zeewain.netty.client.port=9205",
        // "zeewain.netty.client.host=192.168.0.55",
        // "zeewain.netty.client.port=9710",

        // zhy
        // "zeewain.netty.client.host=192.168.30.151",
        // "zeewain.netty.client.port=4443",
        // "zeewain.netty.client.path=/",

        // kong
        // "zeewain.netty.client.host=192.168.0.121",
        // "zeewain.netty.client.port=8000",

        "zeewain.netty.client.host=dev.local.zeewain.com",
        "zeewain.netty.client.port=443",
        "zeewain.netty.client.schema=ws",

        // "zeewain.netty.client.host=ecloud.local.zeewain.com",
        // "zeewain.netty.client.port=6789",

})
public class WebSocketTest extends BaseTest {

    @Autowired
    private HeartBeatClient heartBeatClient;


    @Test
    public void testHeartbeatWithAssert() {
        // Channel channel = clientBuilder.build().getChannel();

        String url = "ws://192.168.0.20:20230/ws/abc";
        // String url = "ws://192.168.0.153:9205/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc&authrization=abc";
        Channel channel = clientBuilder.buildWebsocket(url).getChannel();

        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(channel, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testWebsocketConnected() {
        // Channel channel = clientBuilder.build().getChannel();

        // String url = "ws://localhost:9205/websocket?abc=405";
        // String url = "ws://192.168.0.153:9205/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc&authrization=abc";
        String url = "https://dev.local.zeewain.com/api/openapi-service-netty/websocket?authorization=xx&appId=xx&&sc=voice-asr";

        Channel channel = clientBuilder.buildWebsocket(url).getChannel();

        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(channel, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }



    /**
     * 测试 websocket 通过网关做连接
     * 这个测试需要把父类默认创建的 client 给关闭掉
     */
    @Test
    public void testHeartbeatForBalancer() {
        // String path = "/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=10";
        // /api/wsproxy 这个是kong和nginx的路由
        String path = "wss://dev.local.zeewain.com:443/api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc";
        // String path = "/api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc";
        // String path = "/aiip-debug/api/wsproxy/websocket?upstream=video-fusion-service-netty-nacos&roomId=123";
        NettyClientManager clientManager = clientBuilder.buildWebsocket(path);
        Channel channel = clientManager.getChannel();

        HeartbeatMessage ping = HeartbeatMessage.PING;
        NettyResponse<HeartbeatMessage> resp = heartBeatClient.sendHeartbeat(channel, ping);
        // Assertions.assertEquals(resp.getMsgCode(), HEARTBEAT_RESP);
        Assertions.assertEquals(resp.getData().isPing(), HeartbeatMessage.PONG.isPing());
    }


    @Test
    public void testHeartbeatWithAssert2() throws Exception {
        HeartbeatMessage ping = HeartbeatMessage.PING;
        for (int i = 0; i < 300; i++) {
            Channel channel = clientBuilder.build().getChannel();
            heartBeatClient.sendHeartbeatNoResp(channel, ping);
        }
    }

    @Override
    protected void initMainChannel() {
        // 忽略父类创建 mainChannel
    }


}
