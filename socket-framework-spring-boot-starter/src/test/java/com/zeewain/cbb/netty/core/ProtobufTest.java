package com.zeewain.cbb.netty.core;

import cn.hutool.core.util.RandomUtil;
import com.zeewain.cbb.netty.core.dto.Person;
import com.zeewain.cbb.netty.core.dto.PersonProto;
import com.zeewain.cbb.netty.core.processor.ProtobufClient;
import com.zeewain.cbb.netty.mvc.NettyProcessorManager;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author stan
 * @date 2022/8/26
 */
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "zeewain.netty.client.enable=true",
        "zeewain.netty.client.debug=true"
})
@Slf4j
public class ProtobufTest extends BaseTest {

    @Autowired
    private ProtobufClient protobufClient;


    /**
     * 转换的 protobuf 方式
     * @throws Exception
     */
    @Test
    public void testReq() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Processor<Person> notifyListener = new Processor<Person>() {
            @Override
            public String getMsgCode() {
                return "/protobuf/notify1";
            }

            @Override
            public NettyResponse<Person> process(Person param) {
                param.setName("client " + System.currentTimeMillis());
                latch.countDown();
                return NettyResponse.success(param);
            }
        };
        NettyProcessorManager.register(notifyListener.getMsgCode(), notifyListener);

        PersonProto.Person.newBuilder().build();

        Person person = new Person();
        person.setId(100);
        person.setName("init person");
        NettyResponse<Person> resp = protobufClient.req(mainChannel, person);
        Assertions.assertTrue(resp.isSuccess());
        System.out.println(resp.getData());
        latch.await();
    }/*
// json的大小
17:08:45.244 WRITE: 68B req
17:08:45.287 READ: 80B notify
17:08:45.314 WRITE: 137B notify_resp
17:08:45.318 READ: 134B req_resp

// protobuf 的大小
17:09:41.593 WRITE: 52B req
17:09:41.610 READ: 64B notify
17:09:41.803 WRITE: 134B notify_resp
17:09:41.836 READ: 131B req_resp
*/


    /**
     * 原生 protobuf 的方式
     */
    @Test
    public void testReq2() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Processor<PersonProto.Person> notifyListener = new Processor<PersonProto.Person>() {
            @Override
            public String getMsgCode() {
                return "/protobuf/notify2";
            }

            @Override
            public NettyResponse<PersonProto.Person> process(PersonProto.Person param) {
                PersonProto.Person person = param.toBuilder()
                        .setName("client " + System.currentTimeMillis()).build();
                latch.countDown();
                return NettyResponse.success(person);
            }
        };
        NettyProcessorManager.register(notifyListener.getMsgCode(), notifyListener);

        PersonProto.Person person = PersonProto.Person.newBuilder()
                .setId(100)
                .setName("init person")
                .build();
        NettyResponse<PersonProto.Person> resp = protobufClient.req2(mainChannel, person);
        Assertions.assertTrue(resp.isSuccess());
        System.out.println(resp.getData());
        latch.await();
    }


    @Test
    public void testEmptyResp() {
        PersonProto.Person person = PersonProto.Person.newBuilder()
                .setId(100).setName("from client").build();
        NettyResponse<Void> resp = protobufClient.emptyResp(mainChannel, person);
        System.out.println(resp);
    }


    @Test
    public void testCustomResp() throws Exception {
        PersonProto.Person person = PersonProto.Person.newBuilder()
                .setId(100).setName("from client").build();
        PersonProto.Person person2 = protobufClient.customResp(mainChannel, person);
        System.out.println(person2);
    }


    @Test
    public void testCompress() {
        // String str = "abc";
        // String str = RandomUtil.randomString(1025); // 760B
        // String str = RandomUtil.randomNumbers(1025); // 572B
        // 压缩之后 529B, 没压缩的话 1073B
        String str = RandomUtil.randomString("abcdefg", 1025);
        String str2 = protobufClient.compress(mainChannel, str);
        log.info("发送的字符串 长度={} 内容={}", str.length(), str);
        log.info("响应的字符串 长度={} 内容={}", str.length(), str2);
    }


}
