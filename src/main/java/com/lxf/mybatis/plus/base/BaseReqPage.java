package com.lxf.mybatis.plus.base;

import lombok.Data;

/**
 * @author 小66
 * @create 2020-12-15 20:40
 **/
@Data
public abstract class BaseReqPage {
    private Long current;

    private Long size;
}
