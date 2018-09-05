package homework.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: homework.concurrent.SubThread
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/9/5 21:15
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/9/5      xuecy           v1.0.0               修改原因
 */
public class SubThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SubThread.class);

    private CyclicBarrier cyclicBarrier;

    public SubThread(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        log.info("id:{},name:{},is running", Thread.currentThread().getId(), Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
            cyclicBarrier.await();
            log.info("id:{},name:{},is end,wasting other subThread", Thread.currentThread().getId(), Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
