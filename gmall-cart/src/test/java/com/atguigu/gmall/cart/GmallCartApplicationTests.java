package com.atguigu.gmall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@SpringBootTest
class GmallCartApplicationTests implements Callable<String> {


    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        GmallCartApplicationTests gmallCartApplicationTests = new GmallCartApplicationTests();
        FutureTask<String> stringFutureTask = new FutureTask<>(gmallCartApplicationTests);
        stringFutureTask.run();
        String s = stringFutureTask.get();
        System.out.println("s = " + s);


    }

    @Override
    public String call() throws Exception {
        System.out.println("我是Callable");
        return "callable";
    }

}
