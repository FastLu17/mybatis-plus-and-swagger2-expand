package com.luxf.mybatis.plus.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.luxf.mybatis.plus.base.DescriptionEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.List;

/**
 * Spring MVC配置、
 */
@Configuration
public class WebDataConvertConfig implements WebMvcConfigurer {

    /**
     * 将自定义的Converter注册, 才会生效、
     *
     * @param registry FormatterRegistry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new CustomEnumConverterFactory());
        registry.addConverterFactory(new NumberToEnumConverterFactory());
    }

    /**
     * 处理雪花算法的ID传到前端数据精度丢失的问题.
     * <p>
     * 直接使用converters.add(MappingJackson2HttpMessageConverter),无法生效、
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 处理自定义枚举, 在ResponseBody中, 使用的是Enum#name()返回的问题、以下两种都符合要求.
        simpleModule.addSerializer(DescriptionEnum.class, DescriptionEnumJsonSerializer.instance);

        converters.forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter messageConverter = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = messageConverter.getObjectMapper();
                objectMapper.registerModule(simpleModule);
            }
        });
    }

    /**
     * 由于{@link DescriptionEnum}的实现枚举, toString()方法就是{@link DescriptionEnum#getValue()}, 简单处理 可以直接使用{@link ToStringSerializer#instance}
     */
    private static class DescriptionEnumJsonSerializer extends JsonSerializer<DescriptionEnum> {
        private final static DescriptionEnumJsonSerializer instance = new DescriptionEnumJsonSerializer();
        private final static ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void serialize(DescriptionEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // 不是枚举的时候, 需要单独处理、
            if (!value.getClass().isEnum()) {
                gen.writeString(objectMapper.writeValueAsString(value));
                return;
            }
            // 如果想要返回DescriptionEnum<?>的具体泛型类型, 则稍加处理、
            if (value.getValue() instanceof Number) {
                gen.writeNumber(value.toString());
            } else {
                gen.writeString(value.getValue().toString());
            }
        }
    }
}