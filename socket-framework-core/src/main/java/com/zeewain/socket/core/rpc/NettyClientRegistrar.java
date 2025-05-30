package com.zeewain.socket.core.rpc;

import com.zeewain.socket.core.EnableNettyClients;
import com.zeewain.socket.core.NettyClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Set;

/**
 * 将 @NettyClient 生成对应的 BeanDefinition 注入到容器里边
 *
 * @author stan
 * @date 2022/7/27
 */
public class NettyClientRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;


    /**
     * 根据导入注解提供的元数据， 扫描对应的 bean 注入到 registry
     *
     * @param importingClassMetadata annotation metadata of the importing class， 比如这里是 {@link EnableNettyClients}
     * @param registry               current bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        // 1. 构造扫描器， 去扫描对应的 @NettyClient 标记的类
        AnnotatedBeanScanner scanner = new AnnotatedBeanScanner(environment, this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(NettyClient.class));
        Set<String> basePackages = scanner.getBasePackages(importingClassMetadata);


        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {

                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(),
                            "@NettyClient can only be specified on an interface");

                    // annotation attribute
                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(
                                    NettyClient.class.getCanonicalName());

                    // 2. 将扫描到的信息，注册为 BeanDefinition
                    registerNettyClient(registry, annotationMetadata, attributes);
                }
            }
        }
    }

    private void registerNettyClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        Class clazz = ClassUtils.resolveClassName(className, null);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(NettyClientFactory.class);
        beanDefinitionBuilder.addPropertyValue("type", className);
        beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        beanDefinitionBuilder.setLazyInit(true);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // 提前设置好 FactoryBean 响应的类型，避免根据类型匹配的时候需要实例化对象 造成的性能和提前初始化的影响
        // org.springframework.beans.factory.support.AbstractBeanFactory.getTypeForFactoryBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, boolean)
        beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, clazz);
        registry.registerBeanDefinition(className, beanDefinition);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
