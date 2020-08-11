package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {
    private List<UserAddressEntity> address;

    private List<OrderItemVo> items;

    private Integer bounds;

    private String orderToken;
}
