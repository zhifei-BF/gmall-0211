package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private GmallPmsApi pmsClient;
    @Autowired
    private GmallWmsApi wmsClient;

    @Autowired
    private GmallSmsApi smsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String KEY_PREFIX = "cart:info";


    public void addCart(Cart cart) {

        System.out.println("2");

        // 获取用户的登录信息
        String userId = null;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null){
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }
        String key = KEY_PREFIX + userId;

        // 获取内层map的操作对象（该用户所有购物车的集合）{8: cart, 9:cart}
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 判断该用户购物车中是否已有该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            System.out.println("3-1");
            // 有，更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            // 重新放入redis和mysql数据库
            //this.cartMapper.updateCartByUserIdAndSkuId(userId, cart);
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
        } else {
            System.out.println("3-2");
            // 无，给该用户新增一条记录
            cart.setUserId(userId);
            cart.setCheck(true);

            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null){
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setImage(skuEntity.getDefaultImage());
            }

            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            ResponseVo<List<ItemSaleVo>> listResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = listResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 新增mysql数据库
            System.out.println("4");
            this.cartAsyncService.addCart(cart);
        }
        // redis中，更新和新增都是put方法
        hashOps.put(skuId, JSON.toJSONString(cart));
    }



    public Cart queryCartBySkuId(Long skuId) {
        String key = KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if(userInfo.getUserId()!= null){
            key+=userInfo.getUserId();
        }else {
            key += userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if(hashOps.hasKey(skuId.toString())){
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson,Cart.class);
        }else {
            throw  new RuntimeException("该用户不存在对应的商品的购物车记录");
        }
    }

    public String executor1() {
        try {
            System.out.println("executor1方法开始执行");
            TimeUnit.SECONDS.sleep(4);
            System.out.println("executor1方法结束执行。。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "executor1";
    }

    public String executor2() {
        try {
            System.out.println("executor2方法开始执行");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("executor2方法结束执行。。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "executor2";
    }

    public List<Cart> queryCheckedCarts(Long userId) {
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> cartJsons = hashOps.values();
        if(CollectionUtils.isEmpty(cartJsons)){
            return null;
        }
        return cartJsons.stream().map(cartJson->JSON.parseObject(cartJsons.toString(),Cart.class)).filter(cart->cart.getCheck()).collect(Collectors.toList());
    }


}
