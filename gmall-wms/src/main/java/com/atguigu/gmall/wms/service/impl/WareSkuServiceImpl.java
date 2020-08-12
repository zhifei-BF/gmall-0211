package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX="sotre:lock";




    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public List<SkuLockVO> checkAndLock(List<SkuLockVO> lockVOS) {

        if (CollectionUtils.isEmpty(lockVOS)) {
            return null;
        }

        lockVOS.forEach(lockVO -> {
            // 每一个商品验库存并锁库存
            this.checkLock(lockVO);
        });

        // 如果有一个商品锁定失败了，所有已经成功锁定的商品要解库存
        List<SkuLockVO> successLockVO = lockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
        List<SkuLockVO> errorLockVO = lockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorLockVO)) {
            successLockVO.forEach(lockVO -> {
                this.wareSkuMapper.unlockStock(lockVO.getWareSkuId(), lockVO.getCount());
            });
            return lockVOS;
        }

        // 把库存的锁定信息保存到redis中，以方便将来解锁库存
        String orderToken = lockVOS.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVOS));

        return null; // 如果都锁定成功，不需要展示锁定情况
    }

    private void checkLock(SkuLockVO skuLockVO){
        RLock fairLock = this.redissonClient.getFairLock("lock:" + skuLockVO.getSkuId());
        fairLock.lock();
        // 验库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.checkStock(skuLockVO.getSkuId(), skuLockVO.getCount());
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            skuLockVO.setLock(false); // 库存不足，锁定失败
            fairLock.unlock(); // 程序返回之前，一定要释放锁
            return ;
        }
        // 锁库存。一般会根据运输距离，就近调配。这里就锁定第一个仓库的库存
        if(this.wareSkuMapper.lockStock(wareSkuEntities.get(0).getId(), skuLockVO.getCount()) == 1){
            skuLockVO.setLock(true); // 锁定成功
            skuLockVO.setWareSkuId(wareSkuEntities.get(0).getId());
        } else {
            skuLockVO.setLock(false);
        }
        fairLock.unlock();
    }

}