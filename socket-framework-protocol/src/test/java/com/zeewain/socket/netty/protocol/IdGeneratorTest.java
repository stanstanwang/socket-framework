package com.zeewain.socket.netty.protocol;

import com.zeewain.socket.netty.IdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author stan
 * @date 2022/8/1
 */
class IdGeneratorTest {


    /**
     * 测试自增
     */
    @Test
    void testNew() {
        IdGenerator idGenerator = new IdGenerator();
        Assertions.assertEquals(1, idGenerator.next());
        Assertions.assertEquals(2, idGenerator.next());
    }


    /**
     * 测试超过最大范围后的自增
     */
    @Test
    void testMax() {
        IdGenerator idGenerator = new IdGenerator(Integer.MAX_VALUE);
        Assertions.assertEquals(0, idGenerator.next());
        Assertions.assertEquals(1, idGenerator.next());
    }
}