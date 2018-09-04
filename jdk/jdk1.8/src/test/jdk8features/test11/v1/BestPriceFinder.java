package jdk8features.test11.v1;


import jdk8features.test11.ExchangeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<String> findPricesInUSD(String product) {
        List<CompletableFuture<Double>> priceFutures = new ArrayList<>();
        for (Shop shop : shops) {
            CompletableFuture<Double> futurePriceInUSD =
                    CompletableFuture.supplyAsync(() -> shop.getPrice(product))
//                            combine 组合两个并行的task，等都返回结果之后执行lambda表达式
                            .thenCombine(CompletableFuture.supplyAsync(() -> ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD)),
                                    (price, rate) -> price * rate);

            priceFutures.add(futurePriceInUSD);
        }
        // Drawback: The shop is not accessible anymore outside the loop,
        // so the getName() call below has been commented out.
        List<String> prices = priceFutures
                .stream()
                .map(CompletableFuture::join)
                .map(price -> /*shop.getName() +*/ " price is " + price)
                .collect(Collectors.toList());
        return prices;
    }

    public List<String> findPricesInUSDJava7(String product) {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Double>> priceFutures = new ArrayList<>();
        for (Shop shop : shops) {
            final Future<Double> futureRate = executor.submit(new Callable<Double>() {
                public Double call() {
                    return ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD);
                }
            });
            Future<Double> futurePriceInUSD = executor.submit(new Callable<Double>() {
                public Double call() {
                    try {
                        double priceInEUR = shop.getPrice(product);
                        return priceInEUR * futureRate.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            });
            priceFutures.add(futurePriceInUSD);
        }
        List<String> prices = new ArrayList<>();
        for (Future<Double> priceFuture : priceFutures) {
            try {
                prices.add(/*shop.getName() +*/ " price is " + priceFuture.get());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return prices;
    }


    public List<String> findPricesInUSD2(String product) {
        List<CompletableFuture<String>> priceFutures = new ArrayList<>();
        for (Shop shop : shops) {
            // Here, an extra operation has been added so that the shop name
            // is retrieved within the loop. As a result, we now deal with
            // CompletableFuture<String> instances.
            CompletableFuture<String> futurePriceInUSD =
                    CompletableFuture.supplyAsync(() -> shop.getPrice(product))
                            .thenCombine(
                                    CompletableFuture.supplyAsync(
                                            () -> ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD)),
                                    (price, rate) -> price * rate
                            ).thenApply(price -> shop.getName() + " price is " + price);
            priceFutures.add(futurePriceInUSD);
        }
        List<String> prices = priceFutures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return prices;
    }

    public List<String> findPricesInUSD3(String product) {
        // Here, the for loop has been replaced by a mapping function...
        Stream<CompletableFuture<String>> priceFuturesStream = shops
                .stream()
                .map(shop -> CompletableFuture
                        .supplyAsync(() -> shop.getPrice(product))
                        .thenCombine(
                                CompletableFuture.supplyAsync(() -> ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD)),
                                (price, rate) -> price * rate)
                        .thenApply(price -> shop.getName() + " price is " + price));
        // However, we should gather the CompletableFutures into a List so that the asynchronous
        // operations are triggered before being "joined."
        List<CompletableFuture<String>> priceFutures = priceFuturesStream.collect(Collectors.toList());
        List<String> prices = priceFutures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return prices;
    }

}
