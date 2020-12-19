package com.luxf.mybatis.plus.base;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 小66
 * @create 2020-12-19 16:46
 **/
public enum EnumCache {
    INSTANCE;
    private static final Map<Class<? extends DescriptionEnum<?>>, DescriptionEnum<?>[]> ENUM_CACHE = new ConcurrentHashMap<>(64);

    private static final DescriptionEnum[] EMPTY = new DescriptionEnum[]{};

    public DescriptionEnum<?>[] getEnumValues(Class<? extends DescriptionEnum<?>> enumType) {
        DescriptionEnum<?>[] enums = ENUM_CACHE.get(enumType);
        if (Objects.nonNull(enums)) {
            return enums;
        }
        if (!enumType.isEnum()) {
            return EMPTY;
        }
        DescriptionEnum<?>[] enumConstants = enumType.getEnumConstants();
        ENUM_CACHE.putIfAbsent(enumType, enumConstants);
        return enumConstants;
    }

    public void putValues(Class<? extends DescriptionEnum<?>> key, DescriptionEnum<?>[] val) {
        ENUM_CACHE.putIfAbsent(key, val);
    }
}
