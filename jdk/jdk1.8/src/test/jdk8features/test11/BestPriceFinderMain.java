package jdk8features.test11;


import java.util.List;
import java.util.function.Supplier;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test11.BestPriceFinderMain
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/8 19:49
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/8      xuecy           v1.0.0               修改原因
 */
public class BestPriceFinderMain {
    public static BestPriceFinder  bestPriceFinder = new BestPriceFinder();

    public static void main(String[] args) {
        execute("sequential", () -> bestPriceFinder.findPricesSequential("myPhone27S"));
        execute("parallel", () -> bestPriceFinder.findPricesParallel("myPhone27S"));
//        execute("composed CompletableFuture", () -> bestPriceFinder.findPricesFuture("myPhone27S"));
    }

    public static void execute(String msg, Supplier<List<String>> finder) {
        long start = System.nanoTime();
        System.out.println(finder.get());
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(msg + " done in " + duration + " mescs ");
    }
}
