package com.luxf.mybatis.plus.page.user;

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
 * @since 2020-12-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="User分页查询ReqPage", description="用户表")
public class UserBaseReqPage extends BaseReqPage {

    private String userName;

    private String password;

    private String nickName;

    private Integer age;

    private String address;


}
