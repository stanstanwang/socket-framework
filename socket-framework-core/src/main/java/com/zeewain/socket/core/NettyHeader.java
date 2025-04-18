package com.zeewain.socket.core;


import java.lang.annotation.*;


/**
 * 标记某个参数为从 header 取值，或者设置到 header 中
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyHeader {

    /**
     * 无值的时候表示取整个 map， 有值的时候表示取 map 里边对应某个值绑定到参数
     */
    String value() default "";

}
