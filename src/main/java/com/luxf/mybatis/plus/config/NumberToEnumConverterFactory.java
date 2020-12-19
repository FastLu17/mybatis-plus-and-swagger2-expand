package com.luxf.mybatis.plus.config;

import com.luxf.mybatis.plus.base.DescriptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义枚举转换 NumberToEnumConverterFactory、TODO: 不生效, 很奇怪、
 *
 * @author luxf
 * @see FormatterRegistry#addConverterFactory(ConverterFactory)
 * @see FormatterRegistry#addConverter(Converter)
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class NumberToEnumConverterFactory implements ConverterFactory<Number, DescriptionEnum<?>> {

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
    public <T extends DescriptionEnum<?>> Converter<Number, T> getConverter(Class<T> targetType) {
        return new EnumConverter(getEnumType(targetType));
    }

    private static class EnumConverter<T extends DescriptionEnum<?>> implements Converter<Number, T> {

        private final Class<T> enumType;

        EnumConverter(Class<T> targetType) {
            this.enumType = targetType;
        }

        @Override
        public T convert(Number source) {
            if (source == null) {
                return null;
            }
            // enumType.isEnum() always true.
            T[] enumConstants = enumType.getEnumConstants();
            if (enumConstants != null) {
                ENUM_CACHE.put(enumType, enumConstants);
                for (T constant : enumConstants) {
                    // 需要将DescriptionEnum#getValue()的值转为String、
                    if (source.equals(NumberUtils.parseNumber(constant.getValue().toString(), source.getClass()))) {
                        return constant;
                    }
                }
                throwException(source.toString(), enumConstants);
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