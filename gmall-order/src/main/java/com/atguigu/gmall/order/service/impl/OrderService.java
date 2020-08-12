package com.atguigu.gmall.order.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemVo;

import com.atguigu.gmall.oms.entity.OrderSubmitVO;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * 订单确认页
     * 由于存在大量的远程调用，这里使用异步编排做优化
     * @return
     */
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo==null||userInfo.getUserId()==null)
            throw new UserException("用户未登录");
        Long userId = userInfo.getUserId();


        // 查询送货清单
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> listResponseVo = this.cartClient.queryCheckedCarts(userId);
            List<Cart> carts = listResponseVo.getData();
            if (CollectionUtils.isEmpty(carts)) {
                throw new RuntimeException("没有选中的购物车信息!");
            }
            return carts;
        }, threadPoolExecutor);

        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync(carts -> {
            List<OrderItemVo> items = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount());
                // 根据skuId查询sku
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuEntityResponseVo.getData();
                    orderItemVo.setTitle(skuEntity.getTitle());
                    orderItemVo.setPrice(skuEntity.getPrice());
                    orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                    orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
                }, threadPoolExecutor);
                // 查询销售属性
                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySkuAttrValueBySkuId(cart.getSkuId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                    orderItemVo.setSaleAttrs(skuAttrValueEntities);
                }, threadPoolExecutor);

                // 根据skuId查询营销信息
                CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<ItemSaleVo>> itemSaleVoResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
                    List<ItemSaleVo> itemSaleVos = itemSaleVoResponseVo.getData();
                    orderItemVo.setSales(itemSaleVos);
                }, threadPoolExecutor);

                // 根据 skuId查询库存信息
                CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    }
                }, threadPoolExecutor);
                CompletableFuture.allOf(skuCompletableFuture, saleAttrCompletableFuture, saleCompletableFuture, storeCompletableFuture).join();
                return orderItemVo;
            }).collect(Collectors.toList());

            confirmVo.setItems(items);
        }, threadPoolExecutor);

        // 查询收货地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> addressesResponseVo = this.umsClient.queryAddressesByUserId(userId);
            List<UserAddressEntity> addresses = addressesResponseVo.getData();
            confirmVo.setAddress(addresses);
        }, threadPoolExecutor);

        // 查询用户的积分信息
        CompletableFuture<Void> boundsCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                confirmVo.setBounds(userEntity.getIntegration());
            }
        }, threadPoolExecutor);

        // 防重的唯一标识
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId();
            this.redisTemplate.opsForValue().set(KEY_PREFIX + timeId, timeId);
            confirmVo.setOrderToken(timeId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(itemCompletableFuture, addressCompletableFuture, boundsCompletableFuture, tokenCompletableFuture).join();

        return confirmVo;
    }


    public OrderEntity submit(OrderSubmitVO submitVo) {

        // 1.防重
        String orderToken = submitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if (!flag) {
            throw new RuntimeException("您多次提交过快，请稍后再试！");
        }

        // 2.验价
        BigDecimal totalPrice = submitVo.getTotalPrice(); // 获取页面上的价格
        List<OrderItemVo> items = submitVo.getItems(); // 订单详情
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("您没有选中的商品，请选择要购买的商品！");
        }
        // 遍历订单详情，获取数据库价格，计算实时总价
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResp = this.pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResp.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(item.getCount());
            }
            return new BigDecimal(0);
        }).reduce((t1, t2) -> t1.add(t2)).get();
        if (totalPrice.compareTo(currentTotalPrice) != 0) {
            throw new RuntimeException("页面已过期，刷新后再试！");
        }

        // 3.验库存并锁库存
        List<SkuLockVO> lockVOS = items.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(item.getSkuId());
            skuLockVO.setCount(item.getCount().intValue());
            skuLockVO.setOrderToken(submitVo.getOrderToken());
            return skuLockVO;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVO>> skuLockResp = this.wmsClient.checkAndLock(lockVOS);
        List<SkuLockVO> skuLockVOS = skuLockResp.getData();
        if (!CollectionUtils.isEmpty(skuLockVOS)){
            throw new RuntimeException("手慢了，商品库存不足：" + JSON.toJSONString(skuLockVOS));
        }

        // order：此时服务器宕机

        // 4.下单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderEntity orderEntity = null;
        try {
            ResponseVo<OrderEntity> orderEntityResp = this.omsClient.saveOrder(submitVo, userId);// feign（请求，响应）超时
            orderEntity = orderEntityResp.getData();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果订单创建失败，立马释放库存
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "stock.unlock", orderToken);
        }

        // 5.删除购物车。异步发送消息给购物车，删除购物车
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds", JSON.toJSONString(skuIds));
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "cart.delete", map);

        return orderEntity;
    }
}
