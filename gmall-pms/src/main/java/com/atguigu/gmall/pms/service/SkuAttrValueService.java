package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-20 19:10:10
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId);


    String querySkusJsonBySpuId(Long spuId);


    String querySaleAttrMappingSkuIdBySpuId(Long spuId);

    List<SaleAttrValueVo> queryAllSaleAttrValueBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId);

}

