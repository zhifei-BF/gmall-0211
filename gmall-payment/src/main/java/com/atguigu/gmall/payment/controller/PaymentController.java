package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.entity.PaymentInfoEntity;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("pay.html")
    public String toPay(@RequestParam("orderToken")String orderToken, Model model){
        OrderEntity orderEntity = this.paymentService.queryOrderByOrderToken(orderToken);
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }


    @GetMapping("alipay.html")
    @ResponseBody
    public String alipay(@RequestParam("orderToken") String orderToken){
        try {
            // 校验订单状态
            OrderEntity orderEntity = this.paymentService.queryOrderByOrderToken(orderToken);
            if (orderEntity.getStatus() != 0){
                throw new RuntimeException("此订单无法支付，可能已经过期！");
            }

            // 调用支付宝接口获取支付表单
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            // payVo.setTotal_amount(orderEntity.getPayAmount().toString());
            payVo.setTotal_amount("0.01");
            payVo.setSubject("谷粒商城支付平台");
            // 把支付信息保存到数据库
            Long payId = this.paymentService.save(orderEntity, 1);
            payVo.setPassback_params(payId.toString());
            String form = alipayTemplate.pay(payVo);

            // 跳转到支付页
            return form;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("支付出错，请刷新后重试！");
        }
    }

    @PostMapping("pay/success")
    @ResponseBody
    public String paySuccess(PayAsyncVo payAsyncVo){

        // 1.验签
        Boolean flag = this.alipayTemplate.verifySignature(payAsyncVo);
        if (!flag) {
            //TODO：验签失败则记录异常日志
            return "failure"; // 支付失败
        }

        // 2.验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验
        String payId = payAsyncVo.getPassback_params();
        if (StringUtils.isBlank(payId)){
            return "failure";
        }
        PaymentInfoEntity paymentInfoEntity = this.paymentService.queryPayMentById(Long.valueOf(payId));
        if (paymentInfoEntity == null
                || !StringUtils.equals(payAsyncVo.getApp_id(), this.alipayTemplate.getApp_id())
                || !StringUtils.equals(payAsyncVo.getOut_trade_no(), paymentInfoEntity.getOutTradeNo())
                || paymentInfoEntity.getTotalAmount().compareTo(new BigDecimal(payAsyncVo.getBuyer_pay_amount())) != 0){
            return "failure";
        }

        // 3.校验支付状态。根据 trade_status 进行后续业务处理  TRADE_SUCCESS
        if (!StringUtils.equals("TRADE_SUCCESS", payAsyncVo.getTrade_status())) {
            return "failure";
        }

        // 4.正常的支付成功，记录支付记录方便对账
        paymentService.paySuccess(payAsyncVo);

        // 5.发送消息更新订单状态，并减库存
        this.rabbitTemplate.convertAndSend("order-exchange", "order.pay", payAsyncVo.getOut_trade_no());

        // 6.给支付宝成功回执
        return "success";
    }


    @GetMapping("pay/ok")
    public String payOk(PayAsyncVo payAsyncVo){
        // 查询订单数据展示在支付成功页面
        // String orderToken = payAsyncVo.getOut_trade_no();
        // TODO：查询并通过model响应给页面
        return "paysuccess";
    }


    /**
     * 分布式并发工具类，快速的腾出服务器的资源来处理其他请求；
     * @param skuId
     * @return
     */
    @GetMapping("/miaosha/{skuId}")
    public ResponseVo<Object> kill(@PathVariable("skuId") Long skuId){
        Long userId = LoginInterceptor.getUserInfo().getUserId();
        if(userId!=null){
            // 查询库存
            String stock = this.redisTemplate.opsForValue().get("sec:stock:" + skuId);
            if (StringUtils.isEmpty(stock)){
                return ResponseVo.fail("秒杀结束！");
            }

            // 通过信号量，获取秒杀库存
            RSemaphore semaphore = this.redissonClient.getSemaphore("sec:semaphore:" + skuId);
            semaphore.trySetPermits(Integer.valueOf(stock));
            //0.1s
            boolean b = semaphore.tryAcquire();
            if(b){
                //创建订单
                String orderSn = IdWorker.getTimeId();

                SkuLockVO lockVO = new SkuLockVO();
                lockVO.setOrderToken(orderSn);
                lockVO.setCount(1);
                lockVO.setSkuId(skuId);

                //准备闭锁信息
                RCountDownLatch latch = this.redissonClient.getCountDownLatch("sec:countdown:" + orderSn);
                latch.trySetCount(1);

                this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "sec.kill", lockVO);
                return ResponseVo.ok("秒杀成功，订单号：" + orderSn);
            }else {
                return ResponseVo.fail("秒杀失败，欢迎再次秒杀！");
            }
        }
        return ResponseVo.fail("请登录后再试！");
    }

    @GetMapping("/miaosha/pay")
    public String payKillOrder(String orderSn) throws InterruptedException {

        RCountDownLatch latch = this.redissonClient.getCountDownLatch("sec:countdown:" + orderSn);

        latch.await();

        // 查询订单信息

        return "";
    }

}
