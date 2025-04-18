package com.zeewain.socket.core;


import com.zeewain.socket.protocol.NettyResponse;

import java.lang.annotation.*;

/**
 * 标记某个接口为 netty 的客户端
 * <p>
 * 该接口里边的方法入参和出参有特殊的要求。
 * 入参可以选择 自定义类型 或者 {@link io.netty.channel.Channel}。
 * 出参必须为 {@link NettyResponse}
 *
 * @author stan
 * @date 2022/7/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyClient {

    // 有没有可能 client 和 processor 公用一套接口？？
    // 不太OK， 主要是参数和响应不对，比如会响应异步相关的
    // 也是可以的， 推广异步非阻塞慢慢统一起来就可以了

}
