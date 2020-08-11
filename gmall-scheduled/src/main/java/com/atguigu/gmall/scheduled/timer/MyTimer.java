package com.atguigu.gmall.scheduled.timer;

import java.util.Timer;
import java.util.TimerTask;

public class MyTimer {
    public static void main(String[] args) {
        System.out.println("任务开始时间:" + System.currentTimeMillis());
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("定时任务已经执行了：" + System.currentTimeMillis());
//            }
//        },10000);


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("定时任务已经执行了：" + System.currentTimeMillis());
            }
        },5000,10000);

    }
}
