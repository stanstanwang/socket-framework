package com.zeewain.socket.starter;

import com.zeewain.socket.core.EnableNettyClients;
import com.zeewain.socket.core.NettyProperties;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatClient;
import com.zeewain.socket.core.handle.heartbeat.HeartBeatHandler;
import com.zeewain.socket.core.handle.negotiation.NegotiationClient;
import com.zeewain.socket.core.handle.negotiation.NegotiationProcessor;
import com.zeewain.socket.core.mvc.NettyProcessorPostProcessor;
import com.zeewain.socket.protocol.IdGenerator;
import com.zeewain.socket.protocol.codec.json.JsonCodec;
import com.zeewain.socket.protocol.codec.protobuf.ProtobufCodec;
import com.zeewain.socket.protocol.compress.GzipCompressor;
import com.zeewain.socket.protocol.compress.UnCompressor;
import com.zeewain.socket.core.rpc.NettyRpcResponseHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author stan
 * @date 2022/8/26
 */
@Configuration
@EnableConfigurationProperties(NettyProperties.class)
@EnableNettyClients(basePackageClasses = {HeartBeatClient.class, NegotiationClient.class})
public class NettyCoreConfig {

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }

    @Bean
    public JsonCodec jsonCodec() {
        return new JsonCodec();
    }

    @Bean
    public GzipCompressor gzipCompressor() {
        return new GzipCompressor();
    }

    @Bean
    public UnCompressor unCompressor() {
        return new UnCompressor();
    }

    @Bean
    public ProtobufCodec protobufCodec() {
        return new ProtobufCodec();
    }


    @Bean
    public NettyProcessorPostProcessor nettyProcessorPostProcessor() {
        return new NettyProcessorPostProcessor();
    }


    @Bean
    public NettyRpcResponseHandler nettyRpcResponseHandler() {
        return new NettyRpcResponseHandler();
    }


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public HeartBeatHandler heartBeatHandler(HeartBeatClient heartBeatClient) {
        return new HeartBeatHandler(heartBeatClient);
    }

    @Bean
    public NegotiationProcessor negotiationProcessor(NegotiationClient client) {
        return new NegotiationProcessor(client);
    }

}
