package com.zeewain.socket.netty.core.dto;

import com.zeewain.socket.netty.codec.protobuf.ProtobufConverter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author stan
 * @date 2024/3/1
 */
@Data
public class TtsVo implements ProtobufConverter<PersonProto.TtsVo> {
    @ApiModelProperty(value = "发音人", required = true)
    private String voiceCode;
    @ApiModelProperty("输出的音频格式，可空，可选值 wav 或者 mp3")
    private String format;
    @ApiModelProperty("语速, 0.5、0.8、1、1.2、1.5、1.75、2，默认1")
    private Float speed = 1.0f;
    @ApiModelProperty("音量，1-100 默认50")
    private Integer volume = 50;


    @Override
    public PersonProto.TtsVo convertToProtobuf(Class<PersonProto.TtsVo> type) {
        PersonProto.TtsVo.Builder builder = PersonProto.TtsVo.newBuilder();
        if (this.getFormat() != null) {
            builder.setFormat(this.getFormat());
        }
        if (this.getVoiceCode() != null) {
            builder.setVoiceCode(this.getVoiceCode());
        }
        if (this.getSpeed() != null) {
            builder.setSpeed(this.getSpeed());
        }
        if (this.getVolume() != null) {
            builder.setVolume(this.getVolume());
        }
        return builder.build();
    }

    @Override
    public TtsVo convertFromProtobuf(PersonProto.TtsVo src) {
        this.setFormat(src.getFormat());
        this.setVoiceCode(src.getVoiceCode());
        this.setSpeed(src.getSpeed());
        this.setVolume(src.getVolume());
        return this;
    }

}
