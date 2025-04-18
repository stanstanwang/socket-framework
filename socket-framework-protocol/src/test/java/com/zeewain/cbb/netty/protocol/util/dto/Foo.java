package com.zeewain.cbb.netty.protocol.util.dto;

import com.zeewain.socket.netty.util.LogIgnore;
import lombok.Data;

/**
 * @author stan
 * @date 2025/2/14
 */
@Data
public class Foo {

    @LogIgnore
    private String a;
    private String b;
    private Bar bar;
    private Car car;

    @Data
    public static class Bar {
        private double b;
        @LogIgnore
        private int a;
    }
    @Data
    public static class Car {
        private String a;
        private String b;
    }


}
