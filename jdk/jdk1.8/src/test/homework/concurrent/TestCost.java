package homework.concurrent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

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

    /**
     * 通过任务结束调用barrierAction计算时间
     * public CyclicBarrier(int parties, Runnable barrierAction)
     */
    @Test
    public void testCountTime2() throws InterruptedException {
        long sTime = System.currentTimeMillis();
        int size = 5;
        CyclicBarrier barrier = new CyclicBarrier(size, () -> log.info("{}个任务共用时{}ms", size, System.currentTimeMillis() - sTime));
        ExecutorService executor = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size; i++) {
            executor.execute(new SubThread(barrier));
        }
        Thread.sleep(2000);//等待它们输出
    }


    /**
     * CountDownLatch
     * 通过CountDownLatch 子线程countDown() 主线程await() 阻塞等待,计算任务总时间
     */
    @Test
    public void testCountTime3() {
        long startTime = System.currentTimeMillis();//主线程开始时间
        int threadCount = 5;//线程数
        CountDownLatch cdl = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                long sTime = System.currentTimeMillis();
                long eTime = 0;
                try {
                    Thread.sleep(1000);
                    eTime = System.currentTimeMillis();
                    log.info("Thread,id:{},name:{},time:{}", Thread.currentThread().getId(),
                            Thread.currentThread().getName(), eTime - sTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    cdl.countDown();
                }
            });
        }
        try {
            cdl.await();
            long endTime = System.currentTimeMillis();//主线程结束时间
            log.info("主线程用时：{}ms", endTime - startTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * join
     */
    @Test
    public void testCountTime4() throws InterruptedException {
        long startTime = System.currentTimeMillis();//主线程开始时间
        List<Thread> startList = new LinkedList();
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(new TaskRunnable());
            t.start();
            startList.add(t);
        }

        startList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long endTime = System.currentTimeMillis();//主线程结束时间
        log.info("主线程用时：{}ms", endTime - startTime);
    }

}
