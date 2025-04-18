package com.zeewain.socket.netty.register;

import com.alibaba.cloud.nacos.registry.NacosRegistration;

import java.util.HashMap;
import java.util.Map;

/**
 * netty 的实例注册信息， 用于手动注册
 *
 * @author stan
 * @date 2022/11/25
 */
public class NettyNacosRegistration extends NacosRegistration {

    private final String serviceId;
    private final String host;
    private final int port;

    private final Map<String, String> metadata = new HashMap<>(1);

    public NettyNacosRegistration(String serviceId, String host, int port) {
        super(null, null, null);
        this.serviceId = serviceId + "-netty";
        this.host = host;
        this.port = port;
        metadata.put("is-jwt", Boolean.FALSE.toString());
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
