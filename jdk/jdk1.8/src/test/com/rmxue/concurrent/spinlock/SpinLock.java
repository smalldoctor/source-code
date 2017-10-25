package com.rmxue.concurrent.spinlock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: xuecy
 * @Date: 2017/5/6
 * @RealUser: Chunyang Xue
 * @Time: 07:49
 * @Package: com.rmxue.concurrent.spinlock
 * @Email: 15312408287@163.com
 */
public class SpinLock {
    // 通过ThreadLocal类型的属性，达到每个线程都有自己的predecessor node和current node；然后通过
    // 引用的方式模拟一个队列，链式引用的关系；
    private final ThreadLocal<Node> prev;
    private final ThreadLocal<Node> node;
    private final AtomicReference<Node> tail = new AtomicReference<>(new Node());

    public SpinLock() {
        this.node = new ThreadLocal<Node>() {
            @Override
            protected Node initialValue() {
                return new Node();
            }
        };

        this.prev = new ThreadLocal<Node>() {
            @Override
            protected Node initialValue() {
                return null;
            }
        };
    }

    // 获取锁其实就是相当于predecessor node的locked标识为false,然后current node的locked标识为true
    // lock方法停止自旋并返回,然后当前Thread的方法继续执行lock方法调用之下的代码；如果lock方法一直自旋，
    // 没有返回，相当于获取锁失败，阻塞执行lock方法调用之下的代码。
    // 锁的本质：就是在条件不满足的情况lock方法不返回，阻塞lock方法调用之下代码的执行；
    // 在条件满足的情况lock方法返回，继续执行lock方法调用之下代码的执行；
    public void lock() {
        final Node node = this.node.get();
        node.locked = true;
        // 通过CAS将当前节点插入队列末尾
        // 获取predecessor node,等待predecessor释放锁
        Node pred = this.tail.getAndSet(node);
        this.prev.set(pred);
        // 通过自旋的方式
        while (pred.locked) {

        }
    }

    public void unlock() {
        final Node node = this.node.get();
        node.locked = false;
        this.node.set(this.prev.get());
    }

    private static class Node {
        private volatile boolean locked;
    }

    public static void main(String[] args) throws InterruptedException {
        final SpinLock lock = new SpinLock();
        lock.lock();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                lock.lock();
                System.out.println(Thread.currentThread().getId() + "acquired the lock!");
                lock.unlock();
            }).start();
            Thread.sleep(100);
        }
        System.out.println("main thread unlock");
        lock.unlock();
    }
}
