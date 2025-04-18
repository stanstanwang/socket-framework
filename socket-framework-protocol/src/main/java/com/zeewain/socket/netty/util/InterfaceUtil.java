package com.zeewain.socket.netty.util;

import cn.hutool.core.lang.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内部使用的工具
 *
 * @author stan
 * @date 2023/4/21
 */
public class InterfaceUtil {

    private static final Map<Pair<Class<?>, Class<?>>, Optional<Type>> TYPE_CACHE = new ConcurrentHashMap<>();


    /**
     * 获取当前类指定接口的第一个参数， 处理 A implements Processor<Param> 这种情况
     *
     * @param clazz           当前的 class
     * @param targetInterface 目标的 interface
     */
    public static Optional<Type> getInterfaceFirstArgType(Class<?> clazz, Class<?> targetInterface) {
        Pair<Class<?>, Class<?>> pair = Pair.of(clazz, targetInterface);
        Optional<Type> actualType = TYPE_CACHE.get(pair);
        if (actualType != null) {
            return actualType;
        }

        actualType = Arrays.stream(clazz.getGenericInterfaces())
                .filter(t -> t instanceof ParameterizedType)
                .map(ParameterizedType.class::cast)
                .filter(t -> targetInterface.equals(t.getRawType()))
                .map(t -> t.getActualTypeArguments()[0])
                .findFirst();
        TYPE_CACHE.put(pair, actualType);
        return actualType;
    }
}
