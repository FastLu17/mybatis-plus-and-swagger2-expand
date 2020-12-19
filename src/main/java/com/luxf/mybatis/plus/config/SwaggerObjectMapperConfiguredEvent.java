package com.luxf.mybatis.plus.config;

import cn.hutool.core.util.ClassUtil;
import com.luxf.mybatis.plus.base.DescriptionEnum;
import com.luxf.mybatis.plus.base.EnumCache;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.configuration.ObjectMapperConfigured;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author luxf
 * @date 2020-12-09 13:27
 **/
@Component
@Slf4j
public class SwaggerObjectMapperConfiguredEvent implements ApplicationListener<ObjectMapperConfigured> {
    private final AtomicBoolean onEvent = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ObjectMapperConfigured event) {
        if (!onEvent.getAndSet(true)) {
            Class<?> mainApplicationClass = deduceMainApplicationClass();
            if (Objects.nonNull(mainApplicationClass)) {
                // 只需要执行一次即可、 可以不用监听ObjectMapperConfigured事件, 直接在Spring合适的生命周期中处理也可
                processSwaggerEnumValue(mainApplicationClass.getPackage().getName());
            }
        }
    }

    /**
     * 扫描具有{@link ApiModelProperty}的枚举属性(只扫描{@link DescriptionEnum}的实现类枚举对象), 处理{@link ApiModelProperty#value()}的值
     *
     * @param packageName 需要被扫描的包名、
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void processSwaggerEnumValue(@NonNull String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .filterInputsBy(str -> str != null && str.endsWith(".class"))
                .addScanners(new FieldAnnotationsScanner()));
        Set<Class<? extends DescriptionEnum>> subTypesOf = reflections.getSubTypesOf(DescriptionEnum.class);
        for (Class<? extends DescriptionEnum> subType : subTypesOf) {
            if (subType.isEnum()) {
                Method toString = ClassUtil.getDeclaredMethod(subType, "toString");
                Class<?> declaringClass = toString.getDeclaringClass();
                /**
                 * 枚举类必须重写toString()方法, 否则swagger无法正确展示可用、{@link com.luxf.mybatis.plus.enums.SexEnum}
                 * toString()的具体返回值, 需要配合{@link IStringToEnumConverterFactory,com.luxf.mybatis.plus.base.IEnumSerializer}
                 */
                if (declaringClass.equals(Enum.class)) {
                    throw new UnsupportedOperationException(
                            String.format("Enum [%s] implements Interface [%s] must override method Enum#toString()",
                                    subType.getName(), DescriptionEnum.class.getName()));
                }
            }
        }
        Set<Field> fields = reflections.getFieldsAnnotatedWith(ApiModelProperty.class);

        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            if (fieldType.isEnum() && DescriptionEnum.class.isAssignableFrom(fieldType)) {
                DescriptionEnum<?>[] enumValues = EnumCache.INSTANCE.getEnumValues((Class<? extends DescriptionEnum<?>>) fieldType);
                if (enumValues.length == 0) {
                    return;
                }
                ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
                InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                try {
                    Field memberValues = handler.getClass().getDeclaredField("memberValues");
                    memberValues.setAccessible(true);
                    Map<String, Object> map = (Map<String, Object>) memberValues.get(handler);
                    String joinText = Stream.of(enumValues).map(item -> item.getValue().toString() + ":" + item.getDesc())
                            .collect(Collectors.joining("; ", " [", "]"));
                    Object value = map.get("value");
                    map.put("value", value + joinText);
                    String allowableValues = Stream.of(enumValues).map(e -> e.getValue().toString()).collect(Collectors.joining(","));
                    map.put("allowableValues", allowableValues);
                    memberValues.setAccessible(false);
                } catch (Exception e) {
                    log.error("process @ApiModelProperty properties error.");
                }
            }
        }
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            log.error("deduceMainApplicationClass error.");
        }
        return null;
    }
}
