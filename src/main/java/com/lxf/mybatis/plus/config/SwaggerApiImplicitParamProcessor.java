package com.lxf.mybatis.plus.config;

import com.lxf.mybatis.plus.base.DescriptionEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 全局处理Swagger的{@link ApiImplicitParam}的枚举类问题、
 *
 * @author luxf
 * @date 2020-11-25 14:24
 **/
@Component
@Slf4j
public class SwaggerApiImplicitParamProcessor implements InstantiationAwareBeanPostProcessor {
    @Override
    public boolean postProcessAfterInstantiation(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Api.class)) {
            Method[] declaredMethods = bean.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(ApiOperation.class)) {
                    if (method.isAnnotationPresent(ApiImplicitParams.class)) {
                        ApiImplicitParams annotation = method.getAnnotation(ApiImplicitParams.class);
                        ApiImplicitParam[] params = annotation.value();
                        for (ApiImplicitParam paramAnnotation : params) {
                            processAnnotation(method, paramAnnotation);
                        }
                    } else if (method.isAnnotationPresent(ApiImplicitParam.class)) {
                        ApiImplicitParam annotation = method.getAnnotation(ApiImplicitParam.class);
                        processAnnotation(method, annotation);
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    private void processAnnotation(Method method, @NonNull ApiImplicitParam paramAnnotation) {
        String name = paramAnnotation.name();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            Class<?> methodParamType = parameter.getType();
            if (DescriptionEnum.class.isAssignableFrom(methodParamType) && parameter.getName().equals(name)) {
                DescriptionEnum<?>[] enumValues = CustomEnumConverterFactory.getEnumValues((Class<? extends DescriptionEnum<?>>) methodParamType);
                if (enumValues.length == 0) {
                    return;
                }
                InvocationHandler handler = Proxy.getInvocationHandler(paramAnnotation);
                try {
                    Field memberValues = handler.getClass().getDeclaredField("memberValues");
                    memberValues.setAccessible(true);
                    Map<String, Object> map = (Map<String, Object>) memberValues.get(handler);
                    String joinText = Stream.of(enumValues).map(item -> item.getValue().toString() + ":" + item.getDesc().toString())
                            .collect(Collectors.joining("; ", " [", "]"));
                    Object value = map.get("value");
                    map.put("value", value + joinText);
                    String allowableValues = Stream.of(enumValues).map(e -> e.getValue().toString()).collect(Collectors.joining(","));
                    map.put("allowableValues", allowableValues);
                    map.put("dataTypeClass", Void.class);
                    memberValues.setAccessible(false);
                } catch (Exception e) {
                    log.error("process @ApiImplicitParam properties error.");
                }
            }
        }
    }
}
