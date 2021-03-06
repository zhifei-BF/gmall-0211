package com.atguigu.gmall.oms.entity;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private BigDecimal count;
    private BigDecimal weight;
    private List<SkuAttrValueEntity> saleAttrs;
    private List<ItemSaleVo> sales;
    private Boolean store = false;
}
