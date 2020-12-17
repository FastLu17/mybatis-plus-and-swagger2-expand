package com.luxf.mybatis.plus.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 小66
 * @create 2020-12-15 20:40
 **/
@Data
public abstract class BaseReqPage {
    @ApiModelProperty("当前页")
    private Long current;

    @ApiModelProperty("每页数量")
    private Long size;
}
