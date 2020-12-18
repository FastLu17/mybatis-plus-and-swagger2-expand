package com.luxf.mybatis.plus.req.user;

import java.time.LocalDateTime;

import com.luxf.mybatis.plus.enums.SexEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author luxf
 * @since 2020-12-18
 */
@Data
@ApiModel(value="User请求Req", description="用户表")
public class UserReq implements Serializable {

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "性别")
    private SexEnum sex;

}
