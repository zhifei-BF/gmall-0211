package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.api.GmallCartApi;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public ResponseVo<OrderConfirmVo> confirm(){
        OrderConfirmVo confirmVo = this.orderService.confirm();
        return ResponseVo.ok(confirmVo);
    }
    @Autowired
    GmallCartClient cartClient;

    @GetMapping("test")
    public  String test(){
        this.cartClient.queryCheckedCarts(4L);
        cartClient.test2();
        return "success";
    }
}
