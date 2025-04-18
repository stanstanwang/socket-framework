package com.zeewain.cbb.netty.mvc;

import com.zeewain.cbb.netty.core.Processor;
import com.zeewain.cbb.netty.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 通过反射调用， 实现类似 springmvc 的注解执行功能
 *
 * @author stan
 * @description
 * @date 2022/7/19
 */
public class ReflectionProcessor implements Processor<Object> {

    /**
     * 支持的消息编码
     */
    private final String msgCode;


    /**
     * 支持的消息类型
     */
    private final Type type;

    /**
     * 是否有响应值
     */
    private final boolean hasResponse;

    /**
     * 对应的所有参数类型
     */
    private final Class<?>[] types;


    /**
     * 对应的对象
     */
    private final Object target;

    /**
     * 对应要执行的方法
     */
    private final Method method;


    public ReflectionProcessor(String msgCode, Object target, Method method) {
        this.msgCode = msgCode;
        this.target = target;
        this.method = method;
        this.types = method.getParameterTypes();

        // 非泛型的话这里也是能正常处理的
        type = Arrays.stream(method.getGenericParameterTypes())
                .filter(t -> !SUPPORTED_PARAM_TYPE.contains(t))
                .findFirst().orElse(null);

        this.hasResponse = !(Void.class.equals(method.getReturnType())
                || void.class.equals(method.getReturnType()));
    }

    @Override
    public String getMsgCode() {
        return msgCode;
    }

    @Override
    public Type getType() {
        return type;
    }


    @Override
    public Object processMessage(ChannelHandlerContext ctx, Message message) {
        return invokeMethod(ctx, ctx.channel(), message.getBody(), message.getHeadMap());
    }


    @SneakyThrows
    private Object invokeMethod(Object... params) {
        try {
            return method.invoke(target, resolveArgs(params));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object[] resolveArgs(Object... params) {
        Object[] actualParams = new Object[types.length];
        // 暴力枚举的方式做参数匹配, 可能参数类型是能重复的
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            Object o = null;
            for (Object param : params) {
                // 后边要支持 header 参数 ，得跟 body 入参为 Map 做好区分，使用 @Header 来做好区分。
                // method.getParameterAnnotations()
                if (type.isInstance(param)) {
                    o = param;
                    break;
                }
            }
            actualParams[i] = o;
        }
        return actualParams;
    }

    @Override
    public boolean hasResponse() {
        return hasResponse;
    }

    public Method getMethod() {
        return method;
    }
}
