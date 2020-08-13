package com.atguigu.gmall.payment.vo;

import lombok.Data;

@Data
public class PayVo {

    private String out_trade_no;
    private String total_amount;
    private String subject;
    private String passback_params;



}
