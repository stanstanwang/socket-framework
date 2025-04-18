package com.zeewain.socket.netty.protocol.util;

import com.zeewain.socket.netty.protocol.util.dto.Foo;
import com.zeewain.socket.netty.util.JsonLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author stan
 * @date 2025/2/14
 */
@Slf4j
class JsonLogUtilTest {

    @Test
    void toJsonLog() {
        Foo foo = new Foo();
        foo.setA("aa");
        foo.setB("bb");
        Foo.Bar bar = new Foo.Bar();
        bar.setA(10);
        bar.setB(1.1);
        foo.setBar(bar);

        Foo.Car car = new Foo.Car();
        car.setA("aa");
        car.setB("bb");
        foo.setCar(car);

        log.info("日志处理 {}", JsonLogUtil.toJsonLog(foo));
        // log.info("日志处理2 {}", JsonLogUtil.toJsonLog(car));
    }
}