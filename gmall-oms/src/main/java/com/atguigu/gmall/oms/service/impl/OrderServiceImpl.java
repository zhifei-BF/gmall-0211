package com.atguigu.gmall.oms.service.impl;

import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.entity.OrderItemVo;
import com.atguigu.gmall.oms.entity.OrderSubmitVO;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVO orderSubmitVO, Long userId) {

        // 保存订单
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderSubmitVO, orderEntity);
        orderEntity.setOrderSn(orderSubmitVO.getOrderToken());
        orderEntity.setUserId(userId);
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(orderSubmitVO.getTotalPrice());
        orderEntity.setPayAmount(orderSubmitVO.getTotalPrice());
        orderEntity.setPayType(orderSubmitVO.getPayType());
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVO.getDeliveryCompany());

        this.save(orderEntity);

        // 保存订单详情
        List<OrderItemVo> orderItems = orderSubmitVO.getItems();
        for (OrderItemVo orderItem : orderItems) {
            OrderItemEntity itemEntity = new OrderItemEntity();

            // 订单信息
            itemEntity.setOrderId(orderEntity.getId());
            itemEntity.setOrderSn(orderEntity.getOrderSn());

            // 需要远程查询spu信息 TODO

            // 设置sku信息
            itemEntity.setSkuId(orderItem.getSkuId());
            itemEntity.setSkuName(orderItem.getTitle());
            itemEntity.setSkuPrice(orderItem.getPrice());
            itemEntity.setSkuQuantity(orderItem.getCount().intValue());

            //需要远程查询优惠信息 TODO

            this.orderItemMapper.insert(itemEntity);
        }

        return orderEntity;
    }

}