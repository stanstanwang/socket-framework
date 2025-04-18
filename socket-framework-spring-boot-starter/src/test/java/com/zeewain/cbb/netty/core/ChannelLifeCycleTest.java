package com.zeewain.cbb.netty.core;

import com.zeewain.cbb.netty.client.builder.ConnectionListener;
import com.zeewain.cbb.netty.core.processor.TestClient;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * @author stan
 * @date 2022/8/27
 */

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.client.enable=true",
        "zeewain.netty.client.debug=true",

        // "zeewain.netty.client.host=192.168.0.55",
        // "zeewain.netty.client.port=9710",
})
@ContextConfiguration(classes = ChannelLifeCycleTest.ClientCallback.class)
@Slf4j
public class ChannelLifeCycleTest extends BaseTest {

    @Autowired
    private TestClient testClient;

    @Autowired
    private ClientCallback clientCallback;


    private boolean executed = false;


    @Test
    public void testResp() {
        NettyResponse<Void> resp = testClient.testResp(mainChannel);
        Assertions.assertTrue(resp.isSuccess());
        Assertions.assertTrue(clientCallback.executed);
    }


    @Test
    public void testDoOnConnect() {
        Channel channel = clientBuilder.doOnConnect(c -> {
            executed = true;
            log.info("connected channel {}", c);
        }).build().getChannel();
        Assertions.assertTrue(executed);
    }



    @Test
    public void testConsume() {
        Consumer<String> mockConsumer = mock(Consumer.class);
        String str = "test";
        mockConsumer.accept(str);
        verify(mockConsumer, times(1)).accept(str);
    }


    static class ClientCallback implements ConnectionListener {

        boolean executed = false;

        @Override
        public void connected(Channel channel) {
            log.info("connected");
            executed = true;
            /*Message message = new Message();
            message.setId(1);
            message.setRequest(true);
            message.setMsgCode(HEARTBEAT);
            message.setBody(HeartbeatMessage.PING);
            AbstractNettyRemoting.writeToChannel(channel, message);*/
        }

        @Override
        public void disconnected(Channel channel) {
            log.info("disconnected");
        }
    }

}
