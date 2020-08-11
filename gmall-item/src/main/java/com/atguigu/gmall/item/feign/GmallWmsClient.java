package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {


}
