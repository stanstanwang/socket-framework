package com.zeewain.cbb.netty.core.processor;

import cn.hutool.core.lang.Assert;
import com.zeewain.cbb.netty.core.NettyMapping;
import com.zeewain.cbb.netty.core.NettyProcessor;
import com.zeewain.cbb.netty.core.dto.Foo;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyProcessor
@NettyMapping("/aa/")
@Slf4j
public class TestProcessor {


    @NettyMapping("echo")
    public NettyResponse<Map<String, Object>> echo(Map<String, Object> obj) {
        return NettyResponse.success("ok", obj);
    }


    @NettyMapping("echoList")
    public NettyResponse<List<Object>> echo(List<Object> obj) {
        return NettyResponse.success("ok", obj);
    }


    @NettyMapping("testResp")
    public NettyResponse<Void> testResp() {
        return NettyResponse.success("ok");
    }

    @NettyMapping("testJsonError")
    public NettyResponse<Void> testJsonError(Foo foo) {
        if(foo.getA() == null) {
            System.out.println(1/0);
        }
        return NettyResponse.success();
    }


    @NettyMapping("testError")
    public void testError() {
        System.out.println(1 / 0);
    }

    @NettyMapping("testTimeout")
    public Mono<NettyResponse<Void>> testTimeout() {
        return Mono.just(NettyResponse.success())
                        .delayElement(Duration.ofSeconds(5));
        // 同步会阻塞整个线程，导致第二个请求也超时
        /*ThreadUtil.sleep(5, TimeUnit.SECONDS);
        return NettyResponse.success();*/
    }


    @NettyMapping("testNPE_REQ")
    public NettyResponse<Void> testNPE() {
        throw new NullPointerException();
    }

    @NettyMapping("testCtx")
    public void testCtx(ChannelHandlerContext ctx) {
        Assert.notNull(ctx);
    }

    @NettyMapping("testChannel")
    public void testChannel(Channel channel, Foo foo) {
        Assert.notNull(channel);
        Assert.notNull(foo);
    }


    @NettyMapping("testRespBool")
    public NettyResponse<Boolean> testRespBool() {
        return NettyResponse.success(false);
    }


    // 阻塞的方式
    @NettyMapping("testMono")
    public NettyResponse<Boolean> testMono() throws Exception {
        Thread.sleep(1000);
        return NettyResponse.success(false);
    }


    // 非阻塞的方式
    @NettyMapping("testMono2")
    public Mono<NettyResponse<Boolean>> testMono2() {
        return Mono.just(NettyResponse.success(false))
                // 启动一个新的线程， 延迟1s才将消息往下发
                .delayElement(Duration.ofSeconds(1));
    }


    // 测试mono执行异常的情况
    @NettyMapping("testMonoError")
    public Mono<NettyResponse<Boolean>> testMonoError(Integer timeout) {
        Mono<NettyResponse<Boolean>> resp = Mono.error(new RuntimeException("mock mono error"));
        return resp.delaySubscription(Duration.ofSeconds(timeout));
    }


}
