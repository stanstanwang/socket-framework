package com.zeewain.socket.android.test;

import com.zeewain.socket.android.test.handle.TestClient;
import com.zeewain.socket.core.client.StartupClient;
import com.zeewain.socket.core.handle.negotiation.NegotiationClient;
import com.zeewain.socket.core.handle.negotiation.NegotiationProcessor;
import com.zeewain.socket.protocol.NegotiationInfo;
import com.zeewain.socket.protocol.NettyResponse;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * @author stan
 * @date 2023/9/20
 */
public class NettyClientTest {


    public static void main(String[] args) {
        // 获取 channel 和 发送数据的客户端
        StartupClient startupClient = ClientFactory.get();
        TestClient client = ClientFactory.getSender(TestClient.class);


        // 同步发送等待结果
        Map<String, Object> param = Collections.singletonMap("a", "aa");
        NettyResponse<Map<String, Object>> resp = client.echo(startupClient.getChannel(), param);
        checkResult(resp);

        // 异步发送处理结果
        Mono<NettyResponse<Map<String, Object>>> respMono = client.echoMono(startupClient.getChannel(), param);
        respMono.subscribe(NettyClientTest::checkResult);

        // 测试双向通信
        NegotiationClient negotiationClient = ClientFactory.getSender(NegotiationClient.class);
        NegotiationProcessor negotiationProcessor = new NegotiationProcessor(negotiationClient);
        ClientFactory.registerReceiver(negotiationProcessor);
        negotiationClient.clientHello(startupClient.getChannel(), NegotiationInfo.V1_DEFAULT);
    }

    private static void checkResult(NettyResponse<Map<String, Object>> resp) {
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isSuccess());
        Assertions.assertNotNull(resp.getData());
        Assertions.assertEquals(resp.getData().get("a"), "aa");
    }


}
