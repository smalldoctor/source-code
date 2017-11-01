package com.rmxue.concurrent.jdk8;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TestFutureTask {
    private Callable<Object> callable = () -> {
        System.out.println("test FutureTask............");
        System.out.println("test FutureTask start sleep");
        Thread.sleep(5000 * 5L);
        System.out.println("test FutureTask end sleep");
        return null;
    };

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() {

    }

    /**
     * 对于FutureTask而言，只有run方法执行结束，才是真正的结束；
     * 有可能存在Callable执行结束了，但是执行状态可能是取消（{@link FutureTask#isCancelled()}是true）的；
     * 或者执行cancel之后，但是Callable已经执行完成;
     *
     * @throws InterruptedException
     */
    @Test
    public void testStatus() throws InterruptedException {
        FutureTask<?> futureTask = new FutureTask(callable);
        executorService.submit(futureTask);
        new Thread(() -> {
            System.out.println("cancel thread");
            futureTask.cancel(true);
        }).start();
        Thread.sleep(5000L);
        System.out.println(futureTask.isCancelled());
        Thread.sleep(5000 * 10L);
    }
}
