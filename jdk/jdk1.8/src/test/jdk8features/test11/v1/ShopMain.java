package jdk8features.test11.v1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test11.v1.ShopMain
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/6 16:06
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/6      xuecy           v1.0.0               修改原因
 */
public class ShopMain {
    public static void main(String[] args) {
        Shop shop = new Shop("BestShop");
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Invocation returned after " + invocationTime + " msecs ");
        // while the price of the product is being calculated
        try {
            double price = futurePrice.get();
            System.out.printf("Price is %.2f%n", price);
        } catch (ExecutionException | InterruptedException e) {
            // 封装为运行时异常
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after " + retrievalTime + " msecs");
    }

    public static void doSomethingElse() {
        System.out.println("Doing something else...");
    }
}
