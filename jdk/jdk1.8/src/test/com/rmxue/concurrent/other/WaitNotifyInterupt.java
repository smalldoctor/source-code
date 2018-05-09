package com.rmxue.concurrent.other;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.other.WaitNotifyInterupt
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/23 15:37
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/23      xuecy           v1.0.0               修改原因
 */
public class WaitNotifyInterupt {

    static Object obj = new Object();

    public static void main(String[] args) throws InterruptedException {

        //创建线程
        Thread threadA = new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("---begin---");
                    //阻塞当前线程
                    synchronized (obj) {
                        obj.wait();
                    }
                    System.out.println("---end---");

                } catch (InterruptedException e) {
                    System.out.println("Thread name :" + Thread.currentThread().getName() + " :interrupted status: " + Thread.currentThread().isInterrupted());
                    e.printStackTrace();
                }
            }
        });

        threadA.setName("theadA");
        threadA.start();

        Thread.sleep(1000);

        System.out.println("---begin interrupt threadA---");
        threadA.interrupt();
        System.out.println("---end interrupt threadA---");
    }
}
