package com.atguigu.gmall.item;

import com.atguigu.gmall.item.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Executable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootTest
class GmallItemApplicationTests {


    @Test
    public void test1(){
        CompletableFuture future = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
                int i = 10/0;
                return 1024;
            }
        }).whenCompleteAsync(new BiConsumer<Object, Throwable>() {
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("----------0=" + o.toString());
                System.out.println("----------throwable=" + throwable);
            }
        }).exceptionally(new Function<Throwable, Object>() {


            @Override
            public Object apply(Throwable throwable) {
                System.out.println("Throwable "+ throwable);
                return 666;
            }
        });
        System.out.println(future);
    }

}
