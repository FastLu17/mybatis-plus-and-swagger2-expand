package com.luxf.mybatis.plus.enums;

import com.luxf.mybatis.plus.base.DescriptionEnum;

/**
 * @author 小66
 * @create 2020-12-18 21:20
 **/
public enum SexEnum implements DescriptionEnum<String> {
    MALE("1", "男"),
    FEMALE("2", "女");

    SexEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private final String value;
    private final String desc;

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
