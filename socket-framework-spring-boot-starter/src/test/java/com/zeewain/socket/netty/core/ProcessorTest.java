package com.zeewain.socket.netty.core;

import com.zeewain.socket.netty.core.dto.Foo;
import com.zeewain.socket.netty.core.processor.TestClient;
import com.zeewain.socket.protocol.NettyResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author stan
 * @date 2022/8/26
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.client.enable=true",
        "zeewain.netty.client.debug=true",
        "zeewain.netty.rpcTimeout=3",
})
@Slf4j
public class ProcessorTest extends BaseTest {

    @Autowired
    private TestClient testClient;


    @Test
    public void testEcho() {
        List<Object> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "nick");
        map.put("age", 88);
        list.add(map);
        NettyResponse<List<Object>> resp = testClient.echoList(mainChannel, list);
        Assertions.assertTrue(resp.isSuccess());
        System.out.println(resp.getData());
    }


    @Test
    public void testNPE() {
        NettyResponse<Void> resp = testClient.testNPE(mainChannel);
        Assertions.assertFalse(resp.isSuccess());
        Assertions.assertEquals("NullPointerException", resp.getMessage());
    }


    @Test
    public void testResp() {
        NettyResponse<Void> resp = testClient.testResp(mainChannel);
        Assertions.assertTrue(resp.isSuccess());
    }


    @Test
    public void testJsonError() {
        Foo foo = new Foo();
        foo.setA("aaa");
        NettyResponse<Void> resp = testClient.testJsonError(mainChannel, foo);
        Assertions.assertTrue(resp.isSuccess());
        foo.setA(null);
        NettyResponse<Void> resp2 = testClient.testJsonError(mainChannel, foo);
        Assertions.assertFalse(resp2.isSuccess());
    }

    @Test
    public void testErrorAndNoResp() {
        testClient.testError(mainChannel);
        silentSleep();
    }


    @Test
    public void testArgCtx() {
        testClient.testCtx(mainChannel);
        silentSleep();
    }

    @Test
    public void testArgChannel() {
        Foo foo = new Foo();
        testClient.testChannel(mainChannel, foo);
        silentSleep();
    }


    @Test
    public void testTimeout() throws Exception{
        Assertions.assertThrows(UndeclaredThrowableException.class,
                () -> testClient.testTimeout(mainChannel));
        testClient.testTimeout(mainChannel, Duration.ofSeconds(6));
    }



    @Test
    public void testReconnect() {
        while (true) {
            log.info("client sleep");
            silentSleep();
        }
    }


    @Test
    public void testRespBool() {
        NettyResponse<Boolean> resp = testClient.testRespBool(mainChannel);
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.isSuccess());
        Assertions.assertFalse(resp.getData());
    }


    /**
     * 测试mono
     */
    @Test
    public void testMono() throws Exception {
        Mono<NettyResponse<Boolean>> mono = testClient.testMono2(mainChannel);
        mono.subscribe(resp -> {
            log.info("mono resp {}", resp);
            Assertions.assertNotNull(resp);
            Assertions.assertTrue(resp.isSuccess());
            Assertions.assertFalse(resp.getData());
        });
        log.info("subscribe 不是当前线程阻塞的");
        Thread.sleep(2000);
    }


    @Test
    public void testMonoError() throws Exception {
        Mono<NettyResponse<Boolean>> mono = testClient.testMonoError(mainChannel, 2);
        mono.subscribe(resp -> {
            log.info("mono resp {}", resp);
            Assertions.assertNotNull(resp);
            Assertions.assertFalse(resp.isSuccess());
            Assertions.assertNotNull(resp.getMessage());
        }, e -> {
            log.error("mono error", e);
        });
        log.info("subscribe 不是当前线程阻塞的");
        Thread.sleep(6000);
    }


    private void silentSleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
