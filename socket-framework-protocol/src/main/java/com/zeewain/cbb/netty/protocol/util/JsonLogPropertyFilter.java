package com.zeewain.cbb.netty.protocol.util;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author stan
 * @date 2025/2/14
 */
public class JsonLogPropertyFilter implements PropertyPreFilter {

    private static final Map<Class<?>, Set<String>> excludeMap = new ConcurrentHashMap<>();

    @Override
    public boolean apply(JSONSerializer serializer, Object object, String name) {
        Set<String> excludeNames = MapUtil.computeIfAbsentForJdk8(excludeMap, object.getClass(), this::initExcludeByClass);
        if (excludeNames.isEmpty()) {
            return true;
        }
        return !excludeNames.contains(name);
    }

    private HashSet<String> initExcludeByClass(Class<?> clazz) {
        HashSet<String> set = new HashSet<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(LogIgnore.class)) {
                set.add(declaredField.getName());
            }
        }
        return set;
    }
}
