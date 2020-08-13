package com.atguigu.gmall.payment.config;

import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AlipayTemplate {
    public String pay(PayVo payVo) {
        return null;
    }

    public Boolean verifySignature(PayAsyncVo payAsyncVo) {
        return false;
    }

    public CharSequence getApp_id() {

        return null;
    }
}
