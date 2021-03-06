package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.oms.entity.OrderEntity;

/**
 * 订单
 *
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-22 00:51:19
 */
public interface OrderService extends IService<OrderEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    OrderEntity saveOrder(OrderSubmitVO orderSubmitVO, Long userId);
}

