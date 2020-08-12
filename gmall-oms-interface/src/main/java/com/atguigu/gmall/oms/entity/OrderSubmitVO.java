package com.atguigu.gmall.oms.entity;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private String orderToken;
    private BigDecimal totalPrice;
    private UserAddressEntity address;
    private Integer payType;
    private String deliveryCompany;
    private List<OrderItemVo> items;
    private Integer bounds;

    // 发票信息 TODO

    // 营销信息 TODO
}
