package homework.concurrent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: homework.concurrent.TestCost
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/9/4 10:40
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/9/4      xuecy           v1.0.0               修改原因
 */
public class TestCost {
    private static final Logger log = LoggerFactory.getLogger(TestCost.class);

    /**
     * 方法一
     * awaitTermination 阻塞方法
     */
    @Test
    public void testCountTime1() throws InterruptedException {
        long startTime = System.currentTimeMillis();//主线程开始时间
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.execute(new TaskRunnable());
        }
        executor.shutdown();

        /**
         * Blocks until all tasks have completed execution after a shutdown
         * request, or the timeout occurs, or the current thread is
         * interrupted, whichever happens first.
         */
        executor.awaitTermination(10, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();//主线程结束时间
        log.info("主线程用时：{}ms", endTime - startTime);
    }


}
