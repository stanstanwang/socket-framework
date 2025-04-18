package com.zeewain.socket.netty.core;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author stan
 * @date 2022/8/26
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.websocketServer.enable=true",
        "zeewain.netty.websocketServer.debug=true",
        "zeewain.netty.client.enable=false",
})
@Slf4j
@Deprecated // 整合在一起之后，就没必要了
public class EmbedWebsocketNettyServer extends BaseTest {

    @Test
    public void test() throws Exception {
        while (true) {
            log.info("server sleep");
            TimeUnit.SECONDS.sleep(10);
        }
    }

}
