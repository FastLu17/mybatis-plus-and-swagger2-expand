package com.luxf.mybatis.plus.config;

import com.luxf.mybatis.plus.base.DescriptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.Assert;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义枚举转换、类似 org.springframework.core.convert.support.StringToEnumConverterFactory
 * <p>
 * 在{@link WebMvcConfigurer#addFormatters(FormatterRegistry)}中,添加自定义的Converter、
 *
 * 正常情况下,可以使用Spring提供的StringToEnumConverterFactory即可、无需自定义！(此处自定义是由于前端传参是[1,2,3,4],不是枚举的name()值, 故自定义枚举转换)
 *
 * @author luxf
 * @Configuration
 * public class WebDataConvertConfig implements WebMvcConfigurer {
 *      @Override
 *      public void addFormatters(FormatterRegistry registry) {
 *          registry.addConverterFactory(new CustomEnumConverterFactory());
 *      }
 * }
 * @see FormatterRegistry#addConverterFactory(ConverterFactory)
 * @see FormatterRegistry#addConverter(Converter)
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class CustomEnumConverterFactory implements ConverterFactory<String, DescriptionEnum<?>> {

    /**
     * {@link DescriptionEnum}继承了{@link com.baomidou.mybatisplus.annotation.IEnum}, 可以直接将枚举值转换插入数据库中、
     */
    private static final Map<Class<? extends DescriptionEnum<?>>, DescriptionEnum<?>[]> ENUM_CACHE = new ConcurrentHashMap<>(64);


    static DescriptionEnum<?>[] getEnumValues(Class<? extends DescriptionEnum<?>> enumType) {
        DescriptionEnum<?>[] enums = ENUM_CACHE.get(enumType);
        if (Objects.nonNull(enums)) {
            return enums;
        }
        if (enumType.isEnum()) {
            DescriptionEnum<?>[] enumConstants = enumType.getEnumConstants();
            ENUM_CACHE.put(enumType, enumConstants);
            return enumConstants;
        }
        return new DescriptionEnum[]{};
    }

    @Override
    public <T extends DescriptionEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new EnumConverter(getEnumType(targetType));
    }

    private static class EnumConverter<T extends DescriptionEnum<?>> implements Converter<String, T> {

        private final Class<T> enumType;

        EnumConverter(Class<T> targetType) {
            this.enumType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source.length() == 0) {
                return null;
            }
            // enumType.isEnum() always true.
            T[] enumConstants = enumType.getEnumConstants();
            if (enumConstants != null) {
                ENUM_CACHE.put(enumType, enumConstants);
                for (T constant : enumConstants) {
                    if (source.trim().equals(constant.getValue())) {
                        return constant;
                    }
                }
                throwException(source, enumConstants);
            }
            return null;
        }

        private void throwException(String source, DescriptionEnum<?>[] descriptionEnums) {
            String allValues = Stream.of(descriptionEnums).map(e -> e.getValue().toString()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalArgumentException("param [" + source + "] not in Enum " + enumType.getCanonicalName() + " allValues " + allValues);
        }
    }

    private Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        Assert.notNull(enumType, () -> "The target type " + Objects.requireNonNull(targetType).getName() + " does not refer to an enum");
        return enumType;
    }
}