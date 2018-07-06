package jdk8features.test11.v1;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test11.v1.BestPriceFinder
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/6 20:31
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/6      xuecy           v1.0.0               修改原因
 */
public class BestPriceFinder {
    private final List<Shop> shops =
            Arrays.asList(new Shop("BestPrice"),
                    new Shop("LetsSaveBig"),
                    new Shop("MyFavoriteShop"),
                    new Shop("BuyItAll"), new Shop("ShopEasy"));

    private final Executor executor = Executors.newFixedThreadPool(shops.size(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
//                    守护线程在执行完会被回收；非守护线程处于执行状态，无法被回收
                    t.setDaemon(true);
                    return t;
                }
            });

    public List<String> findPricesSequential(String product) {
        return shops.stream().map(shop -> String.format("%s price is %.2f",
                shop.getName(), shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    public List<String> findPricesParallel(String product) {
        return shops.parallelStream().map(shop -> String.format("%s price is %.2f",
                shop.getName(), shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    public List<String> findPricesFuture(String product) {
        /**
         * 1. 不要连续的调用Map，因为如果map对应的方法是阻塞调用，则反而影响性能；
         * 通过变成两个流，然后进行map；
         * 2. lambda表达式进行局部变量的访问
         * */
//        List<CompletableFuture<String>> futures = shops.stream()
//                .map(shop -> CompletableFuture.supplyAsync(
//                        () -> String.format("%s price is %.2f",
//                                shop.getName(), shop.getPrice(product)))
//                ).collect(Collectors.toList());

//        自定义执行器
        List<CompletableFuture<String>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> String.format("%s price is %.2f",
                                shop.getName(), shop.getPrice(product)), executor)
                ).collect(Collectors.toList());

        List<String> prices = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return prices;
    }
}
