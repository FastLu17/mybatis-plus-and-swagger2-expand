package com.luxf.mybatis.plus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luxf.mybatis.plus.base.BaseEntity;
import com.luxf.mybatis.plus.enums.SexEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author luxf
 * @since 2020-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@ApiModel(value = "User对象", description = "用户表")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户名")
    @TableField("USER_NAME")
    private String userName;

    @ApiModelProperty(value = "密码")
    @TableField("PASSWORD")
    private String password;

    @ApiModelProperty(value = "昵称")
    @TableField("NICK_NAME")
    private String nickName;

    @ApiModelProperty(value = "年龄")
    @TableField("AGE")
    private Integer age;

    @ApiModelProperty(value = "地址")
    @TableField("ADDRESS")
    private String address;

    @ApiModelProperty(value = "性别")
    @TableField("SEX")
    private SexEnum sex;
}
