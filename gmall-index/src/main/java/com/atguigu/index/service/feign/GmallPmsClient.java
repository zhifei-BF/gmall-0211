package com.atguigu.index.service.feign;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
