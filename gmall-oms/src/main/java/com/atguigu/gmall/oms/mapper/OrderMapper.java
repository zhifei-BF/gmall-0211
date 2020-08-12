package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-22 00:51:19
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {


    int closeOrder(String orderToken);


}
