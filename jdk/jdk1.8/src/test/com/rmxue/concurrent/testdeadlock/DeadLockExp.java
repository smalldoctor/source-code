package com.rmxue.concurrent.testdeadlock;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.testdeadlock.DeadLockExp
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/27 10:46
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/27      xuecy           v1.0.0               修改原因
 */
public class DeadLockExp implements Runnable {
    private Resource myOwn, myNeed;

    public DeadLockExp(Resource myOwn, Resource myNeed) {
        this.myOwn = myOwn;
        this.myNeed = myNeed;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            while (true) {
                synchronized (myOwn) {
                    System.out.println("Thread " + threadName + " has got Resource " + myOwn);
                    Thread.sleep(1000);
                    synchronized (myNeed) {
                        System.out.println("Thread " + threadName + " has got Resource " + myNeed);
                        Thread.sleep(1000);
                        System.out.println("Thread " + threadName + "'s job has done.");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }
}
