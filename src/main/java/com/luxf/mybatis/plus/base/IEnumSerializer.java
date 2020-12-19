package com.luxf.mybatis.plus.base;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.Serializable;

/**
 * 特定的枚举{@link DescriptionEnum}序列化器、
 */
public class IEnumSerializer extends JsonSerializer<DescriptionEnum> {

    @Override
    public void serialize(DescriptionEnum anEnum, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 不是枚举的时候
        if (!anEnum.getClass().isEnum()) {
            gen.writeObject(anEnum);
            return;
        }
        // 对Number类型,稍加处理
        Serializable getValue = anEnum.getValue();
        if (getValue instanceof Number) {
            gen.writeNumber(getValue.toString());
        } else {
            gen.writeString(getValue.toString());
        }
    }
}