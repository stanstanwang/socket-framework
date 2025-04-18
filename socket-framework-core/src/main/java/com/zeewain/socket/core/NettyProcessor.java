package com.zeewain.socket.core;

import com.zeewain.socket.netty.NettyResponse;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标记某个方法为 netty 的处理器, 该方法若有响应值，则必须为 {@link NettyResponse}
 *
 * @author stan
 * @date 2022/7/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NettyProcessor {


}
