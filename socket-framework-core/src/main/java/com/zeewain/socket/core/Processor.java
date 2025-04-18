package com.zeewain.socket.core;

import com.zeewain.socket.netty.Message;
import com.zeewain.socket.netty.NettyResponse;
import com.zeewain.socket.netty.util.InterfaceUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author stan
 * @description
 * @date 2022/7/19
 */
public interface Processor<T> {


    List<Type> SUPPORTED_PARAM_TYPE = Arrays.asList(ChannelHandlerContext.class, Channel.class);
    List<Type> SUPPORTED_RETURN_TYPE = Arrays.asList(void.class, Void.class, NettyResponse.class);

    /**
     * 支持的消息类型
     */
    String getMsgCode();

    /**
     * 获取当前Processor的入参类型
     */
    default Type getType() {
        Class<?> clazz = this.getClass();
        Optional<Type> actualType = InterfaceUtil.getInterfaceFirstArgType(clazz, Processor.class);
        if (actualType.isPresent()) {
            return actualType.get();
        } else {
            throw new IllegalArgumentException("Netty Processor 自动推动类型失败");
        }
    }


    /**
     * 是否有响应
     */
    default boolean hasResponse() {
        return true;
    }


    /**
     * 处理业务逻辑
     *
     * @param param 具体的参数
     * @return 响应的结果
     */
    default Object process(T param) {
        throw new IllegalStateException("not yet implemented");
    }


    default Object process(Channel channel, T param) {
        return this.process(param);
    }


    default Object process(ChannelHandlerContext ctx, T param) {
        return this.process(ctx.channel(), param);
    }

    /**
     * 最全的处理器逻辑, 协议解析后，执行器必经的逻辑
     */
    @SuppressWarnings("unchecked")
    default Object processMessage(ChannelHandlerContext ctx, Message message) {
        return this.process(ctx, (T) message.getBody());
    }


}