package com.zeewain.cbb.netty.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Mono;

/**
 * @author stan
 * @date 2023/5/9
 */
@Slf4j
public class ReactorTest {


    @Test
    public void test() {
        Mono.just("abc")
                .subscribe(System.out::println);

        // 消费的时候，再拿结果
        Mono.defer(() -> Mono.just("abc"))
                .subscribe(a -> log.info("a {}", a));
    }

    @Test
    public void testError() {
        Mono.error(new NullPointerException())
                .subscribe(null,
                        e -> System.out.println(e.toString()));
    }

}
