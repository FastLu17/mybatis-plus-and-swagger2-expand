package com.luxf.mybatis.plus.req.user;

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
 * @since 2020-12-17
 */
@Data
@ApiModel(value="User请求Req", description="用户表")
public class UserReq implements Serializable {

    private String userName;

    private String password;

    private String nickName;

    private Integer age;

    private String address;


}
