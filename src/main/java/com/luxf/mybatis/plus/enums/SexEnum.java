package com.luxf.mybatis.plus.enums;

import com.luxf.mybatis.plus.base.DescriptionEnum;

/**
 * @author 小66
 * @create 2020-12-18 21:20
 **/
public enum SexEnum implements DescriptionEnum<Integer> {
    // TODO: 如果是Number类型, 无法转换为正确的Enum、 --> 已完美处理. DescriptionEnum 指定序列化器和反序列化器、
    MALE(1, "男"),
    FEMALE(2, "女");

    SexEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private final Integer value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
