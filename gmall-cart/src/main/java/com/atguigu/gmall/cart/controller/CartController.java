package com.atguigu.gmall.cart.controller;


import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public String addCart(Cart cart){
        if(cart ==null || cart.getSkuId() == null){
            throw new RuntimeException("没有选择添加到购物车的商品信息");
        }
        System.out.println("1");
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String addCart(@RequestParam("skuId")Long skuId, Model model){
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart",cart);
        return "addCart";

    }


    @GetMapping("test")
    @ResponseBody
    public String test(){
        // UserInfo userInfo = LoginInterceptor.getUserInfo();
        // System.out.println(userInfo);
        long now = System.currentTimeMillis();
        System.out.println("controller.test方法开始执行！");
        this.cartService.executor1();
        this.cartService.executor2();
        System.out.println("controller.test方法结束执行！！！" + (System.currentTimeMillis() - now));

        return "hello cart!";
    }


    @GetMapping("test2")
    @ResponseBody
    public String test2() {
        return "hello";
    }


    @GetMapping("query/carts/{userId}")
    @ResponseBody
   public ResponseVo<List<Cart>> queryCheckedCarts(@PathVariable("userId")Long userId){
        List<Cart> carts = this.cartService.queryCheckedCarts(userId);
        return ResponseVo.ok(carts);
    }
}
