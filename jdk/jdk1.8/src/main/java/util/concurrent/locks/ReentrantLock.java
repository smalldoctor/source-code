package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;

/**
 * @Author: xuecy
 * @Date: 2016/10/19
 * @RealUser: Chunyang Xue
 * @Time: 19:33
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */

public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414620L;

    /*Synchronizer是所有实现的基础*/
    private final Sync sync;

    /***
     * ReentrantLock实现同步控制的基础。有公平锁和非公平实现版本。
     * 使用AQS(AbstractQueuedSynchronizer)的states来统计拥有lock的数量
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        abstract void lock();

        /**
         * 非公平的tryLock. tryAcquire方法在子类实现,但是都需要使用非公平用于tryLock方法
         *
         * @param acquires
         * @return
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 说明该锁目前是自由的
                // 首先设置同步器的状态
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 如果同步器的状态不是0,则说明当前锁非自由的,
                // 被某个线程拥有,则判断拥有者是否是当前tryLock的线程
                int nextc = c + acquires;
                if (nextc < 0) {
                    // 因为state是int的,需要判断是会否溢出
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /***
     * 非公平锁Sync
     */
    static final class NonFairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * final实现不可以重写
         */
        final void lock() {

        }
    }

    /**
     * 公平锁
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        @Override
        void lock() {

        }

        /**
         * 遵守公平策略的获取方式;只有是重复获取或者没有waiter或者first才能访问;
         *
         * @param acquires
         * @return
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 该同步器是自由的
                if (!hasQueuedPredecessors() &&
                        compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 当前线程已经持有该同步器
                int nextc = c + acquires;
                if (nextc < 0) {
                    // 因为state是int的,需要判断是会否溢出
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * 创建ReentrantLock实例;
     * 默认是非公平锁
     */
    public ReentrantLock() {
        sync = new NonFairSync();
    }

    /**
     * 根据给定的公平规则创建锁
     *
     * @param fair true 代表创建公平锁
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonFairSync();
    }

    @Override
    public void lock() {
        sync.lock();
    }

    /**
     * 仅在调用此方法时锁未被另一个线程保持的情况下,才获取该锁。
     * <p>
     * 如果该锁未被其他线程拥有时获取该锁并立即返回true,并且设置锁hold count设置为1.
     * 即使使用的是公平策略,在锁是可用的情况下调用tryLock会立即获取锁,无论是否有其他线程正在等待。
     * 在某些情况下,这种闯入行为是有用的,即使会打破公平原则。如果希望遵守公平原则,则可以使用
     * tryLock(long,TimeUtil)方法(tryLock(0,TimeUtil.SECONDS)几乎是等价的),也会检查中断。
     * <p>
     * 如果当前线程已经拥有锁则hold count增加1并且返回true
     * <p>
     * 如果该锁已经被其他线程拥有则立即返回false
     *
     * @return true(如果该锁是自由的并且被当前线程获取, 或者锁已经被当前线程拥有);否则返回false;
     */
    @Override
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    /**
     * 在给定的时间内如果该锁没有被其他线程保持且当前线程未被中断则获取该锁
     * <p>
     * 如果该锁没有被另一个线程保持则获取该锁并立即返回true,且设置hold count为1.
     * 如果该锁被设置为使用公平策略则可用的锁不会被获取(如果有其他线程正在等待该锁);
     * 这与tryLock()方法是刚好相反的。
     * 如果想在公平锁实现闯入那么就组合使用timed和un-timed的形式:
     * if(lock.tryLock() || lock.tryLock(timeout, unit)){
     * <p>
     * }
     * <p>
     * 如果当前线程已经持有锁则hold count增加1,并且立即返回true
     * <p>
     * 如果该锁被其他线程持有,则基于线程调度的目的,当前线程不可用,处于休眠状态,直到如下三种情况:
     * 1. 锁被当前线程获取
     * 2. 其他线程中断当前线程
     * 3. 超时
     * <p>
     * 如果获取到锁,则返回true,且hold count设置为1
     * <p>
     * 如果当前线程在进入这个方法时已经设置中断状态;或者在等待获取锁的过程中被中断,则抛出InterruptedException
     * 并且线程中断状态被清除。
     * <p>
     * 如果超时则返回false;如果设置的时间小于等于0,则此方法根本不会等待。
     * <p>
     * 在具体实现中,因为此方法是一个显示的中断点,所以优先处理中断,而不是普通的锁获取和重入,和超时。
     *
     * @param timeout 锁等待时间
     * @param unit    时间单位
     * @return true:如果锁是自由且被当前线程获取或者锁已经被当前线程持有;
     * false:超时
     * @throws InterruptedException 如果当前线程被中断
     * @throws NullPointerException 如果时间单位为null
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
