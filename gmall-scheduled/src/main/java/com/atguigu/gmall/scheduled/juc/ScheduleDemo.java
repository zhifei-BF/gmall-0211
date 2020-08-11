package com.atguigu.gmall.scheduled.juc;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleDemo {
    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        System.out.println("任务启动时间：" + System.currentTimeMillis());
//        executorService.schedule(()->{
//            System.out.println("这是一个定时任务。" + System.currentTimeMillis());
//        }, 10, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(()->{
            System.out.println("这是一个定时任务。" + System.currentTimeMillis());
        },5,10,TimeUnit.SECONDS);
    }
}
