package com.zeewain.socket.netty.rpc;

import com.zeewain.socket.netty.core.NettyHeader;
import com.zeewain.socket.netty.core.NettyMapping;
import io.netty.channel.Channel;
import org.reactivestreams.Publisher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author stan
 * @date 2022/8/18
 */
public class RpcMethod {
    static final Map<Method, RpcMethod> RPC_METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * 消息编码
     */
    private final String msgCode;

    /**
     * 头部参数的位置, 通过注解标识出来，找参数的时候方便处理
     */
    private final int headerIndex;

    /**
     * 原先的响应类型
     */
    private final Type originType;

    /**
     * 真正的响应类型
     */
    private final Type returnType;


    /**
     * 当前是否异步响应
     */
    private final boolean async;


    public RpcMethod(String msgCode, int headerIndex, Type originType, Type returnType, boolean async) {
        this.msgCode = msgCode;
        this.headerIndex = headerIndex;
        this.originType = originType;
        this.returnType = returnType;
        this.async = async;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public Type getReturnType() {
        return returnType;
    }

    public boolean isAsync() {
        return async;
    }


    public static RpcMethod fromMethod(Method method) {
        NettyMapping mapping = method.getAnnotation(NettyMapping.class);
        if (mapping == null) {
            throw new IllegalStateException(String
                    .format("rpc client method %s not annotated with @NettyMapping", method));
        }

        // 1. 参数校验
        checkParameter(method);
        int headerIndex = findHeaderIndex(method);

        // 2. 响应值处理
        Type returnType = method.getGenericReturnType();
        // async 的话，要拿具体的响应类型才行
        boolean async = isAsync(returnType);
        Type actualReturnType = returnType;
        if (async) {
            actualReturnType = getAsyncInnerType(returnType);
        }

        // 3. 路径映射
        String path = Optional.ofNullable(method.getDeclaringClass()
                        .getAnnotation(NettyMapping.class))
                .map(it -> it.value()[0])
                .orElse("") + mapping.value()[0];
        return new RpcMethod(path, headerIndex, returnType, actualReturnType, async);
    }

    /**
     * 获取异步响应的内部值, 一般都是泛型的第一个参数
     */
    private static Type getAsyncInnerType(Type returnType) {
        Type actualReturnType;
        if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            actualReturnType = pt.getActualTypeArguments()[0];
        } else {
            actualReturnType = Object.class;
        }
        return actualReturnType;
    }

    private static void checkParameter(Method method) {
        int parameterCount = method.getParameterCount();

        if (parameterCount == 0) {
            throw new IllegalArgumentException(String
                    .format("rpc client method should have a Channel parameter : %s", method));
        }

        // 不能超3个参数
        if (parameterCount > 3) {
            throw new IllegalArgumentException(String
                    .format("rpc client method should only have two parameter : %s", method));
        }

        // 其中必定有一个是 Channel 类型
        if (Arrays.stream(method.getParameterTypes())
                .noneMatch(Channel.class::equals)) {
            throw new IllegalArgumentException(String
                    .format("rpc client method must have a parameter Channel : %s", method));
        }
    }

    private static int findHeaderIndex(Method method) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(NettyHeader.class)) {
                if (Map.class.isAssignableFrom(parameter.getType())) {
                    return i;
                } else {
                    throw new IllegalArgumentException(String.format("@NettyHeader must be annotated to Map<String,String> for %s", method));
                }
            }
        }
        return -1;
    }

    private static boolean isAsync(Type returnType) {
        // 去掉泛型
        Class<?> rawClazz;
        if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            rawClazz = (Class<?>) pt.getRawType();
        } else {
            rawClazz = (Class<?>) returnType;
        }

        // 当前是不是异步
        return Future.class.isAssignableFrom(rawClazz)
                || Publisher.class.isAssignableFrom(rawClazz);
    }


    public int getHeaderIndex() {
        return headerIndex;
    }

    public Type getOriginType() {
        return originType;
    }
}
