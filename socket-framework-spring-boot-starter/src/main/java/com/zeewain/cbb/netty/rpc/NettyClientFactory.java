package com.zeewain.cbb.netty.rpc;

import com.zeewain.cbb.netty.core.NettyProperties;
import com.zeewain.cbb.netty.protocol.IdGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.lang.reflect.Proxy;

/**
 * 负责生成 @NettyClient 的代理对象
 *
 * @author stan
 * @date 2022/7/27
 */
public class NettyClientFactory implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    private Class<?> type;

    private ApplicationContext ac;
    private IdGenerator idGenerator;
    private NettyProperties nettyProperties;


    /**
     * 根据接口， 生成当前的代理客户端
     *
     * @param interfaceClass @NettyClient 标注的接口类
     */
    @SuppressWarnings("unchecked")
    public <T> T getClient(Class<T> interfaceClass) {
        ClassLoader loader = interfaceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{interfaceClass};
        Object o = Proxy.newProxyInstance(loader, interfaces, new NettyClientExecutor(idGenerator, nettyProperties.getRpcTimeout()));
        return (T) o;
    }

    @Override
    public Object getObject() {
        init();
        return getClient(type);
    }

    private void init() {
        if (idGenerator == null) {
            idGenerator = ac.getBean(IdGenerator.class);
        }

        if (nettyProperties == null) {
            nettyProperties = ac.getBean(NettyProperties.class);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.type, "type must be set");
    }


    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setNettyProperties(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }
}
