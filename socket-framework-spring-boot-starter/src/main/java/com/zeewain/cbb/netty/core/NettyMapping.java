package com.zeewain.cbb.netty.core;

import com.zeewain.cbb.netty.protocol.NettyResponse;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标记某个方法为 netty 的处理器, 该方法的响应体必须为 {@link NettyResponse} 或者 void
 *
 * @author stan
 * @date 2022/7/19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyMapping {

    @AliasFor("path")
    String[] value() default {};

    @AliasFor("value")
    String[] path() default {};


    // TODO delay stan 2022/7/26 支持不同 processor 使用不同线程池?? 或者给到业务自己处理

}
