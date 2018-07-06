package jdk8features.test11.v1;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static jdk8features.test11.Util.delay;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test11.v1.Shop
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/5 23:53
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/5      xuecy           v1.0.0               修改原因
 */
public class Shop {
    private final String name;
    private final Random random;

    public String getName() {
        return name;
    }

    public Shop(String name) {
        this.name = name;
        this.random = new Random(name.charAt(0) * name.charAt(1) * name.charAt(2));
    }

    //    同步调用，阻塞调用
    public double getPrice(String product) {
        return calculatePrice(product);
    }

    private double calculatePrice(String product) {
        delay();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    //    异步调用，非阻塞调用
    public Future<Double> getPriceAsync(String product) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {
//            目前的方式会导致异常的时候，客户端是无法得知异常的具体原因
            double price = calculatePrice(product);
            futurePrice.complete(price);
        }).start();
        return futurePrice;
    }


    //    异步调用，非阻塞调用
    public Future<Double> getPriceAsync2(String product) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {
            try {
                double price = calculatePrice(product);
                futurePrice.complete(price);
            } catch (Exception ex) {
//                客户端获取异步线程的异常信息
                futurePrice.completeExceptionally(ex);
            }
        }).start();
        return futurePrice;
    }

    public Future<Double> getPriceAsync3(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
    }

}
