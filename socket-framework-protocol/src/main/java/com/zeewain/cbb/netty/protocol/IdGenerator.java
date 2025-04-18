package com.zeewain.cbb.netty.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author stan
 * @description
 * @date 2022/7/26
 */
public class IdGenerator {

    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;

    public IdGenerator() {
        this(0);
    }

    public IdGenerator(int val) {
        this.atom = new AtomicInteger(val);
    }

    // 生成正整数的 [0-2^31) 范围的序列
    // 不用负数是因为不同客户端兼容性不同
    public int next() {
        return atom.incrementAndGet() & MASK;
    }

}
