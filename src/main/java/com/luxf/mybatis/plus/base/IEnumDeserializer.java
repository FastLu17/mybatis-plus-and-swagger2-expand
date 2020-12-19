package com.luxf.mybatis.plus.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 特定枚举{@link DescriptionEnum}的反序列化器
 */
@SuppressWarnings("unchecked")
public class IEnumDeserializer extends JsonDeserializer<DescriptionEnum> {

    @Override
    public DescriptionEnum deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String currentName = p.getCurrentName();
        Object currentValue = p.getCurrentValue();
        Field field = ReflectionUtils.findField(currentValue.getClass(), currentName);
        Class<?> type = Objects.requireNonNull(field).getType();
        String text = p.getText();
        if (type.isEnum() && DescriptionEnum.class.isAssignableFrom(type)) {
            if (!StringUtils.hasLength(text)) {
                return null;
            }
            DescriptionEnum<?>[] enumValues = EnumCache.INSTANCE.getEnumValues((Class<? extends DescriptionEnum<?>>) type);
            for (DescriptionEnum<?> value : enumValues) {
                if (value.getValue().toString().equals(text)) {
                    return value;
                }
            }
            String allValues = Stream.of(enumValues).map(e -> e.getValue().toString()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalArgumentException("the value [" + text + "] of param [" + currentName + "] not in Enum " + type.getCanonicalName() + " allValues " + allValues);
        }
        throw new UnsupportedOperationException("this deserializer only supported subTypes of " + DescriptionEnum.class.getName());
    }
}