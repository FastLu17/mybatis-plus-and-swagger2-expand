package com.luxf.mybatis.plus.base;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * 指定 自定义的序列化器和反序列化器, 完美处理 RequestBody 和 ResponseBody 中枚举的映射、
 *
 * @author luxf
 * @date 2020-12-07 17:35
 **/
@JsonDeserialize(using = IEnumDeserializer.class)
@JsonSerialize(using = IEnumSerializer.class)
public interface DescriptionEnum<V extends Serializable> extends IEnum<V> {
    /**
     * 枚举值的描述
     */
    String getDesc();
}
