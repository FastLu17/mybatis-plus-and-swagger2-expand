package com.luxf.mybatis.plus.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.luxf.mybatis.plus.base.DescriptionEnum;

import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义fastJson枚举类反序列化、
 */
@SuppressWarnings({"unchecked"})
public class CustomEnumDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Class<?> enumType = (Class<?>) type;
        if (enumType.isEnum() && DescriptionEnum.class.isAssignableFrom(enumType)) {
            DescriptionEnum<?>[] enumConstants = (DescriptionEnum<?>[]) enumType.getEnumConstants();
            String input = parser.getInput();
            JSONObject jsonObject = JSONObject.parseObject(input);
            String value = jsonObject.getString(fieldName.toString());
            for (DescriptionEnum<?> descEnum : enumConstants) {
                // toString()方法返回的就是DescriptionEnum#getValue()、
                String descEnumValue = descEnum.toString();
                if (descEnumValue.equals(value)) {
                    return (T) descEnum;
                }
            }
            String allValues = Stream.of(enumConstants).map(Object::toString).collect(Collectors.joining(",", "[", "]"));
            throw new JSONException(String.format("value of fieldName [%s] is [%s], " +
                    "but not in Enum %s allValues %s", fieldName, value, enumType.getCanonicalName(), allValues));
        }
        throw new UnsupportedOperationException("please choose correct EnumDeserializer to deserialize, current deserializer is " + CustomEnumDeserializer.class);
    }

    /**
     * 与{@link EnumDeserializer#getFastMatchToken()}的实现相同
     */
    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}