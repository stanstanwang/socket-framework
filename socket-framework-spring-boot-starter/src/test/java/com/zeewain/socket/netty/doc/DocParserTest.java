package com.zeewain.socket.netty.doc;

import com.zeewain.socket.netty.core.dto.Foo;
import com.zeewain.socket.netty.doc.dto.TypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author stan
 * @date 2022/8/26
 */
public class DocParserTest {

    @Test
    public void parseTypeInfo() {
        DocParser parser = new DocParser(null);
        TypeInfo typeInfo = parser.parseTypeInfo(Foo.class);
        System.out.println(typeInfo);
        Assertions.assertTrue(typeInfo.isObject());
        Assertions.assertSame(2, typeInfo.getTypeInfos().size());
    }
}