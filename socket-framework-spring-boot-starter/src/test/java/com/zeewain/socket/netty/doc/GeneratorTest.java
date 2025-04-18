package com.zeewain.socket.netty.doc;

import com.zeewain.socket.core.doc.DocGenerator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author stan
 * @date 2022/8/26
 */
public class GeneratorTest {


    @Test
    public void test() {
        DocGenerator generator = new DocGenerator();
        generator.setParsePattern("classpath*:com/zeewain/cbb/netty/doc/processor/*.class");
        generator.setOutputPath("src/test/resources/docGenerator.md");
        generator.setIncludeGroups(new HashSet<>(Arrays.asList("aa",
                "aa")));
        /*generator.setIncludePaths(new HashSet<>(Arrays.asList("testVoid",
                "testVoid")));*/
        generator.generate();
    }


}
