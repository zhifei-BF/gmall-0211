package com.atguigu.index.service.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.index.service.feign.GmallPmsClient;

import com.atguigu.index.service.mapper.CategoryMapper;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    public static final String KEY_PREFIX = "index:category";

    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesByPid(1L);
        return listResponseVo.getData();
    }




    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        //从缓存中获取
        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotEmpty(cacheCategories)){
            //如果缓存中有，直接返回
            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return categoryEntities;
        }


        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSub(pid);

        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(listResponseVo),30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }


    public void testLock() {
        //查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        //没有该值return
        if(StringUtils.isBlank(value)){
            return;
        }
        //有值就转成int
        int num = Integer.parseInt(value);
        //把redis中的值加1
        this.redisTemplate.opsForValue().set("num",String.valueOf(++num));
    }
}
