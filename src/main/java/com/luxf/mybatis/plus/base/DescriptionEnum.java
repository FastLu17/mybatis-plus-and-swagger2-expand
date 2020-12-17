package com.luxf.mybatis.plus.base;

import com.baomidou.mybatisplus.annotation.IEnum;

import java.io.Serializable;

/**
 * @author luxf
 * @date 2020-12-07 17:35
 **/
public interface DescriptionEnum<V extends Serializable> extends IEnum<V> {
    /**
     * 枚举值的描述
     */
    String getDesc();
}
