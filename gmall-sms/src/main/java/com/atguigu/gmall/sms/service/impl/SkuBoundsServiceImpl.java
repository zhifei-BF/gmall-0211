package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.service.SkuFullReductionService;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionService skuFullReductionService;

    @Autowired
    private SkuLadderMapper ladderMapper;

    @Autowired
    private SkuFullReductionMapper reductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public void saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
//        // 3.1. 积分优惠
//        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
//        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
//        // 数据库保存的是整数0-15，页面绑定是0000-1111
//        List<Integer> work = skuSaleVo.getWork();
//        if (!CollectionUtils.isEmpty(work)){
//            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3));
//        }
//        this.save(skuBoundsEntity);
//
//        // 3.2. 满减优惠
//        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
//        BeanUtils.copyProperties(skuSaleVo, skuFullReductionEntity);
//        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
//        this.skuFullReductionMapper.insert(skuFullReductionEntity);
//
//        // 3.3. 数量折扣
//        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
//        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
//        this.skuLadderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {

        List<ItemSaleVo> itemSaleVos = new ArrayList<>();
        // 查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        System.out.println(skuBoundsEntity);
        ItemSaleVo bounds = new ItemSaleVo();
        bounds.setType("积分");
        bounds.setDesc("送" + skuBoundsEntity.getGrowBounds() + "成长积分，送" + skuBoundsEntity.getBuyBounds() + "购物积分");
        itemSaleVos.add(bounds);

        // 查询满减信息
        SkuFullReductionEntity reductionEntity = this.reductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        ItemSaleVo reduction = new ItemSaleVo();
        reduction.setType("满减");
        reduction.setDesc("满" + reductionEntity.getFullPrice() + "减" + reductionEntity.getReducePrice());
        itemSaleVos.add(reduction);

        // 查询打折信息
        SkuLadderEntity ladderEntity = this.ladderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        ItemSaleVo ladder = new ItemSaleVo();
        ladder.setType("打折");
        ladder.setDesc("满" + ladderEntity.getFullCount() + "件打" + ladderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
        itemSaleVos.add(ladder);
        return itemSaleVos;
    }


}