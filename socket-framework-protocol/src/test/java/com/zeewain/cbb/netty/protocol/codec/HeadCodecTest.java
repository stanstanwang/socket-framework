package com.zeewain.cbb.netty.protocol.codec;

import com.zeewain.socket.netty.HeadMapSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author stan
 * @date 2022/11/16
 */
public class HeadCodecTest {



    @Test
    public void testGen(){
        HeadMapSerializer simpleMapSerializer = HeadMapSerializer.getInstance();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();

        Map<String,String> map = new HashMap<>();
        // map.put("name", "stan");
        // map.put("age", "18");
        map.put("a", "aa");
        int encode = simpleMapSerializer.encode(map, byteBuf);
        System.out.println(encode);
        System.out.println(byteBuf);
        printBuff(byteBuf);
    }


    private void printBuff(ByteBuf byteBuf){
        byte[] bs = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bs);
        System.out.println(Arrays.toString(bs));
        System.out.println(new String(bs));
    }


}
