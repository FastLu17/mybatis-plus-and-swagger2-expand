package com.luxf.mybatis.plus.service.impl;

import com.luxf.mybatis.plus.entity.User;
import com.luxf.mybatis.plus.mapper.UserMapper;
import com.luxf.mybatis.plus.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author luxf
 * @since 2020-12-18
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
