package com.zeewain.socket.core.util;

import java.util.Map;
import java.util.function.Function;

/**
 * 内部使用的工具
 * @author stan
 * @date 2023/4/18
 */
public class CollectionUtils {


    /**
     * 用来过渡下Jdk1.8下ConcurrentHashMap的性能bug
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">...</a>
     *
     * @param concurrentHashMap ConcurrentHashMap 没限制类型了，非ConcurrentHashMap就别调用这方法了
     * @param key               key
     * @param mappingFunction   function
     * @param <K>               k
     * @param <V>               v
     * @return V
     * @since 3.4.0
     */
    public static <K, V> V computeIfAbsent(Map<K, V> concurrentHashMap, K key, Function<? super K, ? extends V> mappingFunction) {
        V v = concurrentHashMap.get(key);
        if (v != null) {
            return v;
        }
        return concurrentHashMap.computeIfAbsent(key, mappingFunction);
    }
}
