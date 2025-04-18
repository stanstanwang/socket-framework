package com.zeewain.socket.netty.doc.processor;

import com.zeewain.socket.core.NettyClient;
import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.netty.core.dto.Foo;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyClient
@NettyMapping("aa/")
@Api("aa")
public interface DocClient {

    @NettyMapping("testError")
    void testError(Channel channel);

    @NettyMapping("testCtx")
    void testCtx(Channel channel);

    @NettyMapping("testChannel")
    void testChannel(Channel channel, Foo foo);

}
