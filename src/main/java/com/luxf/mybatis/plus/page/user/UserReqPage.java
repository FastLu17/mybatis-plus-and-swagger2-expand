package com.luxf.mybatis.plus.page.user;

import java.time.LocalDateTime;
import com.luxf.mybatis.plus.base.BaseReqPage;
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
@ApiModel(value="User分页查询ReqPage", description="用户表")
public class UserReqPage extends BaseReqPage {

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

    @ApiModelProperty(value = "性别,1:男 2:女")
    private String sex;

    private String createdBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;


}
