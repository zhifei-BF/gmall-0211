package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderSubmitVO;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.service.impl.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;



    @GetMapping("confirm")
    public String confirm(Model model){
        OrderConfirmVo confirmVo = this.orderService.confirm();
        model.addAttribute("confirmVo",confirmVo);
        return "trade";
    }

    /**
     * 提交订单返回订单id
     * @param orderSubmitVO
     * @return
     */
    @PostMapping("submit")
    @ResponseBody
    public ResponseVo<Object> submit(@RequestBody OrderSubmitVO submitVo){

        OrderEntity orderEntity = this.orderService.submit(submitVo);
        return ResponseVo.ok(orderEntity.getOrderSn());
    }
}
