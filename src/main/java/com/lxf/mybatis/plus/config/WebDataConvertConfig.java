package com.lxf.mybatis.plus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        converters.forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter messageConverter = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = messageConverter.getObjectMapper();
                objectMapper.registerModule(simpleModule);
            }
        });
    }
}