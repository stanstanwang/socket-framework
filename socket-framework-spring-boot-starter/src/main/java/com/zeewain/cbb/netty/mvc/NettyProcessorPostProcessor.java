package com.zeewain.cbb.netty.mvc;

import com.zeewain.cbb.netty.core.NettyMapping;
import com.zeewain.cbb.netty.core.NettyProcessor;
import lombok.SneakyThrows;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 解析 {@link NettyProcessor}
 * <p>
 * - 获取所有注解 NettyProcessor 的方法
 * - 解析方法，生成 ReflectionProcessor
 * - 注入管理
 *
 * @author stan
 * @description
 * @date 2022/7/20
 */
public class NettyProcessorPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (targetClass.isAnnotationPresent(NettyProcessor.class)) {
            parse(bean, targetClass);
            return bean;
        }
        return bean;
    }


    /**
     * 内部单测的时候注册使用，用于解析类型
     */
    @SneakyThrows
    public void _innerRegister(Class<?> targetClass) {
        this.parse(targetClass.newInstance(), targetClass);
    }

    @SneakyThrows
    public void _innerRegister(Object processor) {
        this.parse(processor, processor.getClass());
    }


    private void parse(Object bean, Class<?> targetClass) {
        // 支持父路径
        String parentPath = Optional.ofNullable(targetClass
                        .getAnnotation(NettyMapping.class))
                .map(it -> it.value()[0]).orElse("");

        for (Method method : targetClass.getDeclaredMethods()) {
            NettyMapping nettyMapping = method.getAnnotation(NettyMapping.class);
            if (nettyMapping == null) {
                continue;
            }
            // find corresponded method for aop execution
            Method actualMethod = findActualMethod(bean, method);
            List<String> paths = Arrays.stream(nettyMapping.value())
                    .map(it -> parentPath + it)
                    .collect(Collectors.toList());
            parseMethod(bean, actualMethod, paths);
        }
    }

    private void parseMethod(Object bean, Method method, List<String> paths) {
        // 目前先限定死 第一个参数可支持 Context
        int parameterCount = method.getParameterCount();
        if (parameterCount > 3) {
            throw new IllegalArgumentException("@NettyProcessor 注解的方法暂不支持超过3个参数");
        }

        // 响应值的校验
        // 20230418 支持任意响应， 因为 protobuf 响应类型不好限定， 另外 springmvc 这个响应类型也是没有限制的
        /*Class<?> returnType = method.getReturnType();
        if (!Processor.SUPPORTED_RETURN_TYPE.contains(returnType)) {
            throw new IllegalArgumentException(String.format(
                    "@NettyProcessor 不支持的响应类型 %s, 方法 %s",
                    returnType.getName(), method));
        }*/

        // 构造对应的 Processor
        for (String msgCode : paths) {
            ReflectionProcessor reflectionProcessor = new ReflectionProcessor(
                    msgCode, bean, method
            );
            NettyProcessorManager.register(msgCode, reflectionProcessor);
        }
    }


    /**
     * 找到代理bean中的真正方法
     */
    private Method findActualMethod(Object bean, Method method) {
        return Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(m -> {
                    if (!m.getName().equals(method.getName())) {
                        return false;
                    }

                    if (m.getParameterCount() != method.getParameterCount()) {
                        return false;
                    }

                    Class<?>[] t1 = m.getParameterTypes();
                    Class<?>[] t2 = method.getParameterTypes();
                    for (int i = 0; i < m.getParameterCount(); i++) {
                        if (!t1[i].equals(t2[i])) {
                            return false;
                        }
                    }

                    return true;
                }).findFirst()
                .orElseThrow(NullPointerException::new);
    }


}
