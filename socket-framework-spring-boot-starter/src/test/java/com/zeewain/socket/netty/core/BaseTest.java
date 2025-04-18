package com.zeewain.socket.netty.core;

import com.zeewain.socket.netty.EnableNettyClients;
import com.zeewain.socket.netty.NettyClientAutoConfig;
import com.zeewain.socket.netty.NettyServerAutoConfig;
import com.zeewain.socket.netty.client.builder.NettyClientBuilder;
import com.zeewain.socket.netty.server.NettyServer;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.PostConstruct;


/**
 * 主要给客户端使用
 *
 * @author stan
 * @date 2022/7/21
 */
@ContextConfiguration(classes = {
        NettyServerAutoConfig.class,
        NettyClientAutoConfig.class
})
@TestPropertySource(properties = {
        "zeewain.netty.server.enable=false",
        "zeewain.netty.server.debug=false",
        "zeewain.netty.client.enable=true",
        "zeewain.netty.client.debug=true"
})
@EnableNettyClients
@ComponentScan
public class BaseTest {


    @Autowired(required = false)
    private NettyServer nettyServer;

    @Autowired(required = false)
    protected NettyClientBuilder clientBuilder;

    protected Channel mainChannel;

    @PostConstruct
    public void _init() {
        // server 端，才有这个类
        // 另外这里是 非 springboot 的单测，所以只能自己启动了
        if (nettyServer != null) {
            nettyServer.start();
        }

        // client 端才有这个类
        initMainChannel();
    }

    protected void initMainChannel() {
        if (clientBuilder != null) {
            mainChannel = clientBuilder.build().getChannel();
        }
    }

}
