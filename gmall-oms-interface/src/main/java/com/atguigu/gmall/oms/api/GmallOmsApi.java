package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderSubmitVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {
    @PostMapping("oms/order/{userId}")
    public ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVO orderSubmitVO, @PathVariable("userId")Long userId);


    @GetMapping("oms/order/token/{orderSn}")
    public ResponseVo<OrderEntity> queryOrderByOrderSn(@PathVariable("orderSn")String orderSn);
}
