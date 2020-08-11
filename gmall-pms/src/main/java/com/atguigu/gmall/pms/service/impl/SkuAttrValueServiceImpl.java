package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;

import static java.util.stream.Collectors.groupingBy;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private SkuAttrValueMapper attrValueMapper;

    @Autowired
    private SkuMapper skuMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
        List<AttrValueVo> attrValueVos = attrValueMapper.querySkuAttrValuesBySpuId(spuId);
        // 以attrId进行分组
        Map<Long, List<AttrValueVo>> map = attrValueVos.stream().collect(groupingBy(AttrValueVo::getAttrId));

        // 创建一个List<SaleAttrValueVo>
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId, attrs) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            // attrId
            saleAttrValueVo.setAttrId(attrId);
            // attrName
            saleAttrValueVo.setAttrName(attrs.get(0).getAttrName());
            // attrValues
            Set<String> attrValues = attrs.stream().map(AttrValueVo::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
            saleAttrValueVos.add(saleAttrValueVo);
        });

        return saleAttrValueVos;
    }

    @Override
    public String querySkusJsonBySpuId(Long spuId) {
        List<Map<String,Object>> skus =  this.attrValueMapper.querySkusJsonBySpuId(spuId);
        Map<String,Long> map =  skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(),sku->(Long) sku.get("sku_id")));
        return JSON.toJSONString(map);

    }

    @Override
    public String querySaleAttrMappingSkuIdBySpuId(Long spuId) {
        List<Map<String, Object>> maps = this.attrValueMapper.querySaleAttrMappingSkuIdBySpuId(spuId);
        System.out.println(maps);
        Map<String, Long> skusMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map -> (Long) map.get("sku_id")));
        return JSON.toJSONString(skusMap);
    }

    @Override
    public List<SaleAttrValueVo> queryAllSaleAttrValueBySpuId(Long spuId) {
        // 先根据spuId查询所有的sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }

        // 2.获取sku的id组成新的集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        // 3.根据skuIds查询所有的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }
        // 根据attrId分组：key-attrId value-相同attrId的SkuAttrValueEntity对象组成的list
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        // map中的每一个kv接口变成：SaleAttrValueVo {attrId: 4, attrName: 内存, attrValues: [8G, 12G]}
        map.forEach((attrId, attrValueEntities) -> {
            // 设置attrId
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            if (!CollectionUtils.isEmpty(attrValueEntities)){
                // 如果集合不为空，取第一个元素中的attrName设置到vo对象中
                saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());

                // 为了防止出现：白色 白色 白色 白色 金色 金色。。。。，要去重
                Set<String> set = attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
                saleAttrValueVo.setAttrValues(set);
            }
            saleAttrValueVos.add(saleAttrValueVo);
        });

        return saleAttrValueVos;
    }


    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {
        return this.attrValueMapper.querySearchAttrValueBySkuId(skuId);
    }

}