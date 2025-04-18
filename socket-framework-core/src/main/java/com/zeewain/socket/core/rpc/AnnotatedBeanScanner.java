package com.zeewain.socket.core.rpc;

import cn.hutool.core.util.StrUtil;
import com.zeewain.socket.core.EnableNettyClients;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnnotatedBeanScanner extends ClassPathScanningCandidateComponentProvider {

    public AnnotatedBeanScanner(Environment environment, ResourceLoader resourceLoader) {
        super(false, environment);
        this.setResourceLoader(resourceLoader);
    }

    // 重写该方法主要是为了接口也能扫描
    @Override
    protected boolean isCandidateComponent(
            AnnotatedBeanDefinition beanDefinition) {
        boolean isCandidate = false;
        if (beanDefinition.getMetadata().isIndependent()) {
            if (!beanDefinition.getMetadata().isAnnotation()) {
                isCandidate = true;
            }
        }
        return isCandidate;
    }


    /**
     * 根据当前在处理的注解找到要扫描的路径
     *
     * @param importingClassMetadata 注解的信息
     * @return 要扫描的路径
     */
    public Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableNettyClients.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StrUtil.isNotEmpty(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StrUtil.isNotEmpty(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class<?>[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

}