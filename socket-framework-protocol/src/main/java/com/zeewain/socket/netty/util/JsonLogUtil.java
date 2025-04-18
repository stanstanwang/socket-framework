package com.zeewain.socket.netty.util;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.zeewain.socket.netty.NettyResponse;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;

/**
 * 内部使用的一个 json 工具类
 *
 * @author stan
 * @date 2023/4/14
 */
public class JsonLogUtil {

    private static final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

    private final static JsonLogPropertyFilter jsonLogPropertyFilter = new JsonLogPropertyFilter();

    /**
     * 可以支持将 protobuf 转换为 json
     */
    @SneakyThrows
    public static String toJsonString(Object message) {
        // NettyResponse 封装 protobuf 得这样子输出，比较麻烦
        if (message instanceof NettyResponse) {
            NettyResponse<?> resp = (NettyResponse<?>) message;
            if (resp.getData() instanceof MessageOrBuilder) {
                return printer.print((MessageOrBuilder) resp.getData());
            }
        }

        // 直接输出 protobuf
        if (message instanceof MessageOrBuilder) {
            return printer.print((MessageOrBuilder) message);
        }

        // mono/flux 这种的日志输出
        if (message instanceof Publisher) {
            return message.getClass().toString();
        }

        // 剩下按之前的方式输出
        return toJsonLog(message);
    }


    public static String toJsonLog(Object message) {
        return JSON.toJSONString(message, jsonLogPropertyFilter);
    }


}
