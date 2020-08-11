package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-20 19:10:10
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {

    List<AttrValueVo> querySkuAttrValuesBySpuId(Long spuId);


    List<Map<String, Object>> querySkusJsonBySpuId(Long spuId);


    List<Map<String, Object>> querySaleAttrMappingSkuIdBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId);


}
