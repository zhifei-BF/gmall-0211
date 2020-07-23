package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-22 01:09:45
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
