package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {
    private String keyword;

    private List<Long> brandId;

    private Long cid;

    private List<String> props;

    private Integer sort = 0;

    private Double priceFrom;
    private Double priceTo;

    private Integer pageNum = 1;//页码
    private final Integer pageSize = 20;//每页记录数

    private Boolean store;//是否有货
}
