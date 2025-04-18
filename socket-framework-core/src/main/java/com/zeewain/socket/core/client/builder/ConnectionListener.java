package com.zeewain.socket.core.client.builder;

import io.netty.channel.Channel;
import org.springframework.core.Ordered;

/**
 * netty作为client的连接回调
 *
 * @author stan
 * @date 2023/5/15
 */
public interface ConnectionListener extends Ordered {
    default void connected(Channel channel) {

    }

    default void disconnected(Channel channel) {
    }


    /**
     * 当前回调的优先级， 数字越小优先级越高
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
