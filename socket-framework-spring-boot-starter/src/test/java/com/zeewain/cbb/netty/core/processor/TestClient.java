package com.zeewain.cbb.netty.core.processor;

import com.zeewain.cbb.netty.core.NettyClient;
import com.zeewain.cbb.netty.core.NettyMapping;
import com.zeewain.cbb.netty.core.dto.Foo;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import io.netty.channel.Channel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyClient
@NettyMapping("/aa/")
public interface TestClient {

    @NettyMapping("echo")
    NettyResponse<Map<String, Object>> echo(Channel channel, Map<String, Object> obj);

    @NettyMapping("echoList")
    NettyResponse<List<Object>> echoList(Channel channel, List<Object> obj);


    @NettyMapping("testResp")
    NettyResponse<Void> testResp(Channel channel);

    @NettyMapping("testJsonError")
    NettyResponse<Void> testJsonError(Channel channel, Foo foo);

    @NettyMapping("testError")
    void testError(Channel channel);


    @NettyMapping("testCtx")
    void testCtx(Channel channel);

    @NettyMapping("testChannel")
    void testChannel(Channel channel, Foo foo);

    // 声明的时候才不抛未命名异常
    @NettyMapping("testTimeout")
    public NettyResponse<Void> testTimeout(Channel channel) /* throws TimeoutException*/;
    @NettyMapping("testTimeout")
    public NettyResponse<Void> testTimeout(Channel channel, Duration duration);


    @NettyMapping("testNPE_REQ")
    NettyResponse<Void> testNPE(Channel channel);


    @NettyMapping("testRespBool")
    NettyResponse<Boolean> testRespBool(Channel channel);


    @NettyMapping("testMono")
    Mono<NettyResponse<Boolean>> testMono(Channel channel);

    @NettyMapping("testMono2")
    Mono<NettyResponse<Boolean>> testMono2(Channel channel);


    @NettyMapping("testMonoError")
    Mono<NettyResponse<Boolean>> testMonoError(Channel channel, Integer timeout);
}
