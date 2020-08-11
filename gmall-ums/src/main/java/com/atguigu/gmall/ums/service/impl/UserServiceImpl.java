package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        // 1.先根据登录名查询用户信息
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>().eq("username", loginName)
                .or().eq("phone", loginName)
                .or().eq("email", loginName));

        // 2.判空
        if (userEntity == null) {
            //throw new IllegalArgumentException("用户名不合法！");
            return userEntity;
        }

        // 3.获取用户信息中的盐，并对用户输入的明文密码加盐加密

        password = DigestUtils.md5Hex(password + userEntity.getSalt());
        System.out.println(password);

        // 4. 比较数据库中保存的密码和用户输入的密码（加密）
        if (!StringUtils.equals(password, userEntity.getPassword())) {
            //throw new IllegalArgumentException("密码不合法！");
            System.out.println("password1"+password);
            System.out.println("password2"+userEntity.getPassword());
            return null;
        }

        return userEntity;
    }

}