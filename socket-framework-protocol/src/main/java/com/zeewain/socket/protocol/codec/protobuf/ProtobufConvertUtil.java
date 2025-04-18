package com.zeewain.socket.protocol.codec.protobuf;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

/**
 * java dto 和 message 互换的工具类，有几种方式
 * 1. json
 * 2. 递归反射
 * 3. 硬编码， 硬编码性能是最高的，借助插件一键生成转换代码也比较方便。
 *
 * @author stan
 * @date 2023/4/21
 */
public class ProtobufConvertUtil {


    private static final ConcurrentMap<Class<?>, Method> methodCache = new ConcurrentReferenceHashMap<>();
    private static final JsonFormat.Parser jsonParser = JsonFormat.parser();
    private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();


    public static Message.Builder getMessageBuilder(Class<?> clazz) throws Exception {
        Method method = methodCache.get(clazz);
        if (method == null) {
            // protobuf 通用构造器就是这个
            method = clazz.getMethod("newBuilder");
            methodCache.put(clazz, method);
        }
        return (Message.Builder) method.invoke(clazz);
    }


    /**
     * 将 java 的 dto 转换为 protobuf
     *
     * @param src  java 的 dto
     * @param type protobuf 的类型
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T extends Message> T convertToProtobufByJson(Object src, Class<T> type) {
        Message.Builder builder = getMessageBuilder(type);
        String json = JSON.toJSONString(src);
        jsonParser.merge(json, builder);
        return (T) builder.build();
    }


    /**
     * 将 protobuf 的 Message 转换为 Java 的 Dto
     *
     * @param src  protobuf 的 Message
     * @param type java 的 dto
     */
    @SneakyThrows
    public static <T> T convertFromProtobufByJson(Message src, Type type) {
        String json = jsonPrinter.print(src);
        return JSON.parseObject(json, type);
    }


    /**
     * 【硬编码的方式】将 java 的 dto 转换为 protobuf
     *
     * @param src  java 的 dto
     * @param type protobuf 的类型
     */
    public static <T extends Message> T convertToProtobufByCode(ProtobufConverter<T> src, Class<T> type) {
        return src.convertToProtobuf(type);
    }

    /**
     * 【硬编码的方式】将 protobuf 的 Message 转换为 Java 的 Dto
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T convertFromProtobufByCode(Message message, Class<T> clazz) {
        if (isConverterType(clazz)) {
            ProtobufConverter<Message> converter = (ProtobufConverter<Message>) clazz.newInstance();
            return (T) converter.convertFromProtobuf(message);
        }
        throw new IllegalArgumentException(String.format("class should be ProtobufConverter : %s", clazz.getName()));
    }

    public static boolean isProtobufType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Message.class.isAssignableFrom(clazz);
    }

    public static boolean isProtobufInstance(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof Message;
    }

    public static boolean isConverterType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return ProtobufConverter.class.isAssignableFrom(clazz);
    }

    static boolean isConverterInstance(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof ProtobufConverter;
    }
}
