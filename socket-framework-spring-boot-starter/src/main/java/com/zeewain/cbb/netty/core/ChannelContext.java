package com.zeewain.cbb.netty.core;

import io.netty.channel.ChannelHandlerContext;

/**
 * netty 当前上下文的封装器， 如果当前是服务端的话， 这个上下文则是客户端的Channel
 * 如果当前是客户端的话， 这个 Channel 是当前客户端的 Channel
 *
 * @author stan
 * @date 2022/7/27
 */
public class ChannelContext {

    // fix stan 2022/7/27 netty 上下文初始化， 目前暂未实现
    // 另外编码习惯应该是让 service 解耦掉 ctx 的依赖？？ 这样是否正常
    private static final ThreadLocal<ChannelHandlerContext> LOCAL = new ThreadLocal<>();

    public static ChannelHandlerContext get() {
        return LOCAL.get();
    }

    public static void set(ChannelHandlerContext str) {
        LOCAL.set(str);
    }

    public static void remove() {
        LOCAL.remove();
    }

}
