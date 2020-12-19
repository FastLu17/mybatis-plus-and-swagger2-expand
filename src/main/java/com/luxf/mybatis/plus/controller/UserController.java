package com.luxf.mybatis.plus.controller;

import cn.hutool.core.lang.Snowflake;
import com.luxf.mybatis.plus.base.Result;
import com.luxf.mybatis.plus.entity.User;
import com.luxf.mybatis.plus.enums.SexEnum;
import com.luxf.mybatis.plus.req.user.UserReq;
import com.luxf.mybatis.plus.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author luxf
 * @since 2020-12-18
 */
@RestController
@RequestMapping("/user")
@Api(value = "用户表", tags = "用户表管理")
@Validated
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private Snowflake idWorker;

    /**
     * 新增 User
     *
     * @param req req
     * @return Result
     */
    @PostMapping
    @ApiOperation(value = "新增 用户表")
    public Result<User> create(@Valid @RequestBody UserReq req) {
        User entity = new User();
        BeanUtils.copyProperties(req, entity);
        Long id = idWorker.nextId();
        entity.setId(id);
        boolean saved = userService.save(entity);
        return saved ? Result.success(entity) : Result.failed();
    }

    /**
     * 根据ID删除 User
     *
     * @param id id
     * @return Result
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据ID删除 用户表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, paramType = "path"),
    })
    public Result<?> deleteById(@PathVariable Long id) {
        boolean removed = userService.removeById(id);
        return removed ? Result.success() : Result.failed();
    }

    /**
     * 根据ID修改 User
     *
     * @param id id
     * @return Result
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "根据ID修改 用户表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, paramType = "path"),
    })
    public Result<?> updateById(@PathVariable Long id, @Valid @RequestBody UserReq req) {
        User entity = new User();
        BeanUtils.copyProperties(req, entity);
        entity.setId(id);
        boolean updated = userService.updateById(entity);
        return updated ? Result.success() : Result.failed();
    }

    /**
     * 根据ID查询 User
     *
     * @param id id
     * @return Result
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询 用户表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, paramType = "path"),
    })
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * TODO: 响应的值,不是SexEnum#value、而是Enum#name().  需要ResponseBody 处理一下、或者自定义EnumToStringConverter? 或者处理ObjectMapper?
     * <p>
     * 已处理、
     *
     * @param sex
     * @return
     * @see com.luxf.mybatis.plus.config.WebDataConvertConfig#configureMessageConverters(List)
     */
    @GetMapping("/enum")
    @ApiOperation(value = "测试Enum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sex", value = "性别", required = true, paramType = "query"),
    })
    public Result<SexEnum> testEnum(@NonNull @RequestParam SexEnum sex) {
        System.out.println("sex = " + sex);
        return Result.success(sex);
    }

    /**
     *  TODO: POST请求时, 数字转换Enum不正常、
     */
    @PostMapping("/enum/post")
    @ApiOperation(value = "测试Enum")
    public Result<UserReq> testEnumPost(@Valid @RequestBody UserReq req) {
        System.out.println("sex = " + req);
        return Result.success(req);
    }
}

