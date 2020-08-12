package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;

import java.util.List;

/**
 * 商品库存
 *
 * @author helin
 * @email 1548617474@qq.com
 * @date 2020-07-23 00:53:44
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuLockVO> checkAndLock(List<SkuLockVO> lockVos);




}

