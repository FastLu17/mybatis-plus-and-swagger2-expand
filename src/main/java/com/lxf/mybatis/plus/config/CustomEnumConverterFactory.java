package com.lxf.mybatis.plus.config;

import com.lxf.mybatis.plus.base.DescriptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
     * {@link DescriptionEnum}继承了{@link com.baomidou.mybatisplus.core.enums.IEnum}, 可以直接将枚举值转换插入数据库中、
     */
    private static final Map<Class<? extends DescriptionEnum<?>>, DescriptionEnum<?>[]> ENUM_CACHE = new ConcurrentHashMap<>(64);


    static DescriptionEnum<?>[] getEnumValues(Class<? extends DescriptionEnum<?>> enumType) {
        DescriptionEnum<?>[] enums = ENUM_CACHE.get(enumType);
        if (Objects.nonNull(enums)) {
            return enums;
        }
        Method values = ClassUtils.getMethod(enumType, "values");
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            values.setAccessible(true);
            return null;
        });
        DescriptionEnum<?>[] invokeResult = new DescriptionEnum<?>[0];
        try {
            invokeResult = (DescriptionEnum<?>[]) values.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("The target type {} invoke method 'values()' error.", enumType);
        }
        ENUM_CACHE.put(enumType, invokeResult);
        return invokeResult;
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

            DescriptionEnum<?>[] descriptionEnums = ENUM_CACHE.get(enumType);
            if (Objects.nonNull(descriptionEnums)) {
                for (DescriptionEnum<?> desc : descriptionEnums) {
                    if (source.trim().equals(desc.getValue())) {
                        return (T) desc;
                    }
                }
                throwException(source, descriptionEnums);
            }
            try {
                Method values = ClassUtils.getMethod(enumType, "values");
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    values.setAccessible(true);
                    return null;
                });
                T[] temporaryConstants = (T[]) values.invoke(null);
                if (temporaryConstants != null) {
                    ENUM_CACHE.put(enumType, temporaryConstants);
                    for (T constant : temporaryConstants) {
                        if (source.trim().equals(constant.getValue())) {
                            return constant;
                        }
                    }
                    throwException(source, temporaryConstants);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("The target type {} invoke method 'values()' error.", enumType);
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