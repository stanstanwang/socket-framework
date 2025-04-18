package com.zeewain.cbb.netty.register;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.annotation.PreDestroy;

/**
 * 存在 nacos 注册中心的话，将 netty 服务注册到注册中心， 这样才能负载均衡
 *
 * @author stan
 * @date 2022/11/25
 */
@Slf4j
public class NettyAutoRegister {

    // 依赖
    // spring-cloud-alibaba-nacos-discovery-2.2.0.RELEASE
    // spring-cloud-commons-2.2.0.RELEASE-sources

    private final ServiceRegistry<Registration> serviceRegistry;
    private final Registration registration;

    public NettyAutoRegister(NacosServiceRegistry serviceRegistry,
                             NacosDiscoveryProperties discoveryProperties, int port) {
        this.serviceRegistry = serviceRegistry;
        this.registration = new NettyNacosRegistration(
                discoveryProperties.getService(),
                discoveryProperties.getIp(),
                port
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    // lower than com.zeewain.cbb.netty.server.NettyServer.start
    @Order(value = 100)
    public void register() {
        serviceRegistry.register(registration);
        log.info("netty nacos registry, {} {} {} register finished", registration.getServiceId(),
                registration.getHost(), registration.getPort());
    }


    @PreDestroy
    public void deregister() {
        serviceRegistry.deregister(registration);
        log.info("netty nacos deregistry, {} {} {} dregister finished", registration.getServiceId(),
                registration.getHost(), registration.getPort());
    }

}


