package com.zeewain.socket.android.test.handle;

import com.zeewain.socket.core.NettyClient;
import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import reactor.core.publisher.Mono;

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


    @NettyMapping("echo")
    Mono<NettyResponse<Map<String, Object>>> echoMono(Channel channel, Map<String, Object> obj);

    @NettyMapping("echo")
    Promise<NettyResponse<Map<String, Object>>> echoPromise(Channel channel, Map<String, Object> obj);
}
