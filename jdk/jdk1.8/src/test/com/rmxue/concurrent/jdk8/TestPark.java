package com.rmxue.concurrent.jdk8;

import java.util.concurrent.locks.LockSupport;

public class TestPark {
    public static void main(String[] args) {
        long beforeParking = System.currentTimeMillis();
        LockSupport.parkNanos(new Object(), 1000000000 * 5L);
        System.out.println("Parking time:" + (System.currentTimeMillis() - beforeParking));
        /**
         * 通过LockSupport.parkNanos挂起的线程，不会设置中断状态;
         */
        System.out.println("the state of the thread after calling parkNanos : " + Thread.currentThread().isInterrupted());
    }
}
