package com.atguigu.gmall.payment.vo;

import lombok.Data;

@Data
public class PayAsyncVo {
    private String passback_params;
    private CharSequence app_id;
    private CharSequence out_trade_no;
    private char[] buyer_pay_amount;
    private CharSequence trade_status;



}
