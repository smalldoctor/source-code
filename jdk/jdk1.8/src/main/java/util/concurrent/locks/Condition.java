package java.util.concurrent.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xuecy
 * @Date: 2016/10/19
 * @RealUser: Chunyang Xue
 * @Time: 19:00
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */
public interface Condition {
    void await() throws InterruptedException;

    void awaitUninterruptibly();

    long awaitNanos(long nanosTimeout) throws InterruptedException;

    boolean await(long time, TimeUnit unit) throws InterruptedException;

    boolean awaitUntil(Date deadline) throws InterruptedException;

    void signal();

    void signalAll();
}
