package java.util.concurrent.locks;

/**
 * @Author: xuecy
 * @Date: 2016/10/19
 * @RealUser: Chunyang Xue
 * @Time: 20:35
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */

import sun.misc.Unsafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 各种同步机制的抽象,如信号量,闭锁,周期障碍,锁,在多线程场景下,以共享状态实现同步,
 * 在符合某个条件时做什么事情
 * <p>
 * 提供一个框架用于实现blocking lock和相关的基于先进先出的队列Synchronizer。
 * AbstractQueuedSynchronizer被用于以atomic int做为状态的同步器的基础。
 * 使用受保护的方法改变state,并且需要定义好什么状态代表获取,什么状态代表释放。
 * 使用getState,setState,compareAndSetState来使用state。
 * <p>
 * AQS对状态的维护是由子类实现，本身主要完成线程阻塞队列的维护和线程的阻塞及唤醒工作；
 * AQS提供了模版方法acquire和release方法；但是具体的实现由子类实现：
 * tryAcquire,tryAcquireShared,tryRelease和tryReleaseShared
 * <p>
 * AQS及基于AQS实现的同步器多数都是用到了一个Spin Lock的机制。Spin Lock就是让线程通过
 * 不断的循环进行等待而不是睡眠。
 *
 * @see com.rmxue.concurrent.spinlock.SpinLock
 * // 获取锁其实就是相当于predecessor node的locked标识为false,然后current node的locked标识为true
 * // lock方法停止自旋并返回,然后当前Thread的方法继续执行lock方法调用之下的代码；如果lock方法一直自旋，
 * // 没有返回，相当于获取锁失败，阻塞执行lock方法调用之下的代码。
 * // 锁的本质：就是在条件不满足的情况lock方法不返回，阻塞lock方法调用之下代码的执行；
 * // 在条件满足的情况lock方法返回，继续执行lock方法调用之下代码的执行；
 * <p>
 * AQS操作的基础是CAS；
 * @since 1.5
 */
public class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer
        implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    private final static boolean compareAndSetWaitStatus(Node node, int expect, int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
    }

    /**
     * 初始化一个状态为0的Synchronizer
     */
    protected AbstractQueuedSynchronizer() {

    }

    /**
     * 实现Lock的lock方法。独占模式。
     * 至少调用tryAcquire一次，如果tryAcquire返回失败，则线程将被放入队列；
     * <p>
     * 互斥获取是不响应中断，即不会抛出中断异常；但是会设置线程的中断状态；
     */
    public final void acquire(int arg) {
        // 调用一次tryAcquire，如果失败则放入queue
        // 如果获取失败，则进入阻塞队列阻塞线程;线程会有可能被反复的唤醒或者阻塞，直到tryAcquire成功;
        if (!tryAcquire(arg)
                && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
            // 因为在第一次获取tryAcquire失败之后，线程则进入阻塞队列，从阻塞队列成功返回（true）之后，说明
            // 是被中断唤醒之后成功获取锁,重置线程的中断状态;如果从碰巧入队之后，立刻成功获取到锁，则返回false，则
            // 是非中断唤醒；
            selfInterrupt();
        }
    }

    /**
     * 互斥获取锁，支持中断异常；
     *
     * @param arg
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        // 首先检查线程的中断状态
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (!tryAcquire(arg)) {
            doAcquireInterruptibly(arg);
        }
    }

    /**
     * Acquires in shared interruptible mode.
     *
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
            throws InterruptedException {
        Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        // 因为到此处head节点已经变成node，原始的head节点 p已经可以被回收
                        p.next = null;
                        failed = false;
                        return;
                    }
                }

                if (shouldParkAfterFailedAcquire(p, node)
                        && parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        Node node = addWaiter(Node.SHARED);
        long deadline = System.nanoTime() + nanosTimeout;

        boolean failed = true;
        try {
            for (; ; ) {
                Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null;
                        failed = false;
                        return true;
                    }
                }

                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node)
                        && nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    private void doAcquireInterruptibly(int arg)
            throws InterruptedException {
        Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (; ; ) {
                Node pred = node.predecessor();
                if (pred == head && tryAcquire(arg)) {
                    setHead(node);
                    pred.next = null;
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(pred, node)
                        && parkAndCheckInterrupt()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 线程会被反复的唤醒或者阻塞，直到tryAcquire成功;
     * 独占模式，忽视中断
     * <p>
     * 1. 只有自己是下个获取锁的节点才去尝试获取锁，即head的下个节点
     *
     * @param node
     * @param arg
     * @return true 如果排队获取锁的过程中，被中断唤醒
     */
    final boolean acquireQueued(final Node node, int arg) {
        // 是否获取成功
        boolean failed = true;
        try {
            boolean interrupted = false;
            // 等待前继节点释放锁
            // 自旋re-check
            for (; ; ) {
                // 获取前继节点
                final Node p = node.predecessor();
                // 前继节点是head，则说明next节点是当前线程，则尝试获取锁
                if (p == head && tryAcquire(arg)) {
                    // head节点就是当前获取到锁的节点
                    setHead(node);
                    p.next = null;
                    failed = false;
                    return interrupted;
                }
                // p != head 或者 tryAcquire失败，则尝试挂起线程；
                if (shouldParkAfterFailedAcquire(p, node)
                        && parkAndCheckInterrupt()) {
                    // 如果是中断返回的
                    interrupted = true;
                }
            }
        } finally {
            if (failed) {
                // 由于异常，如果获取失败
                cancelAcquire(node);
            }
        }
    }

    /**
     * 共享模式;
     * park时被中断，不处理中断;
     *
     * @param arg
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0) {
            doAcquireShared(arg);
        }
    }

    /**
     * 支持中断的共享模式获取
     *
     * @throws InterruptedException
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        // 先检查中断标志, 如果中断则抛出中断异常，同时清除中断标志
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireInterruptibly(arg);
    }

    /**
     * 当是head之后的第一个节点则尝试获取锁
     *
     * @param arg
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                // 如果node没有prev节点是异常场景
                Node pred = node.predecessor();
                if (pred == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        pred.next = null;
                        failed = false;
                        if (interrupted) {
                            // 因为 park是支持中断，但是不会抛出异常，所以检查是否中断，如果是则重置中断状态
                            selfInterrupt();
                        }
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(pred, node)
                        && parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * @param arg
     * @return 如果为负数则获取失败，如果为0则后续获取无法成功，如果为大于0后续获取有可能成功
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param arg
     * @param nanosTimeout
     * @return true 成功获取； false 超时
     * @throws InterruptedException 支持中断异常
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
                doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * 取消 ongoing attempt to acquire
     */
    private void cancelAcquire(Node node) {
        if (node == null) {
            return;
        }

        node.thread = null;

        // 跳过取消状态的前继节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            // 因为在入队等待获取锁时，是判断前继节点的前继节点的
            node.prev = pred = pred.prev;

        Node predNext = pred.next;

        node.waitStatus = Node.CANCELLED;

        // 如果当前节点是tail节点，则可以移除所有waitStatues=CANCELLED的节点
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // node不是tail
            int ws;
            if (pred != head &&
                    ((ws = pred.waitStatus) == Node.SIGNAL
                            || (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL)))
                    && pred.thread != null) {
                // 进行当前取消节点的前置节点的后置节点的指针设置
                Node next = node.next;
                if (next != null && next.waitStatus <= 0) {
                    compareAndSetNext(pred, predNext, next);
                }
            } else {
                // 唤醒线程，线程被唤醒之后还是需要再次获取锁；获取锁有可能会再次失败
                unparkSuccessor(node);
            }

            node.next = node;//help GC
        }
    }

    private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }

    /**
     * 当前线程会在此方法阻塞;
     * 有可能因为unpark或者中断返回
     *
     * @return
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /**
     * Returns a string identifying this synchronizer, as well as its state.
     * The state, in brackets, includes the String {@code "State ="}
     * followed by the current value of {@link #getState}, and either
     * {@code "nonempty"} or {@code "empty"} depending on whether the
     * queue is empty.
     *
     * @return a string identifying this synchronizer, as well as its state
     */
    public String toString() {
        int s = getState();
        String q = hasQueuedThreads() ? "non" : "";
        return super.toString() +
                "[State = " + s + ", " + q + "empty queue]";
    }

    /**
     * 检查线程在获取锁失败之后，是否需要被park；
     * <p>
     * 1. 在挂起等待的时候，会将前一个节点的状态设置为SIGNAL，但是存在设置失败的可能
     *
     * @param pred
     * @param node
     * @return
     */
    private static final boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL) {
            // 前一个节点状态已经设置为SIGNAL（代表需要主动的UNPARK下个节点的线程），因此可以安全的PARK
            return true;
        }
        //
        if (ws > 0) {
            // 前继节点cancelled；则跳过当前节点的前继节点，retry前继节点的前继节点
            do {
                // 此处并没有回避prev是head的场景
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // waitStatus是0(初始化)或者PROPAGATE；
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        /**
         * 在JUC的实现中，都不是直接进行挂起，而是进行过关键操作或者有一定耗时的操作之后都会
         * 进行一次是否满足可以不阻塞或者不挂起的条件的判断（如是否成功获取锁，是否获取到异步计算的结果）
         */
        return false;
    }

    /**
     * 等待队列的头,懒初始化(队列新建时是null)。除了初始化,只能使用setHead方法进行修改。
     * 如果是队列的头,需要保证它的waitState不能是Cancelled。
     * <p>
     * head表示已经出队的线程,在锁的场景下，则是已经持有锁的线程；
     * 通常所说的阻塞队列，是由Node组成的链表，但是不包括head在内；
     * <p>
     * volatile 多线程
     */
    private transient volatile Node head;

    /**
     * 等待队列的tail node,懒初始化(队列新建时是null),只能通过enq追加。
     */
    private transient volatile Node tail;

    /**
     * Synchronizer的state;
     * 在锁实现的场景中，如果等于0，则代表没有持有锁；大于0则代表有线程持有锁；
     * <p>
     * volatile 多线程
     */
    private volatile int state;

    /**
     * 获取当前Synchronizer的state
     * 内存同步的volatile read
     *
     * @return 当前Synchronizer的state
     */
    protected final int getState() {
        return this.state;
    }

    /**
     * 设置Synchronizer的状态;
     * 内存同步的volatile write
     *
     * @param newState
     */
    protected final void setState(int newState) {
        this.state = newState;
    }

    /**
     * 中断当前线程,一个便捷方法
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * 利用底层CAS进行同步的更新;
     * 这个操作具有Volatile的内存语义
     *
     * @param expect
     * @param update
     * @return true 如果更新成功则是true,否则就是false
     */
    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // Queuing utilities

    static final long spinForTimeoutThreshold = 1000L;

    /**
     * CAS head.此方法只能enq使用;只有head为null;
     *
     * @param update 新node
     * @return
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail;只能用于enq;
     *
     * @param expect
     * @param update
     * @return
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    //队列相关

    /**
     * 插入新的Node进入队列;入队操作;
     * 对新队列需要进行head的初始化
     *
     * @param node 新插入的Node
     * @return 当前node的前node
     */
    private Node enq(final Node node) {
        //因为多线程,cas修改存在修改失败,因此需要循环
        for (; ; ) {
            Node t = tail;
            if (t == null) {
                // 因为初始队列tail是空的;此处其实head为null;因为tail为空时,head必然为空;此时必须先进行初始化;
                if (compareAndSetHead(new Node())) {
                    tail = head;
                }
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 创建当前线程的Node并enq
     * (入队操作)
     *
     * @return
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        // tail不为空,说明已经initialized
        // 先通过CAS操作快速入队；如果快速入队失败，则进入循环入队
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        // 快速enq失败之后,只能full enq
        enq(node);
        return node;
    }

    //-----------------------------------setHead

    /**
     * 用于共享模式
     *
     * @param node
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head;
        setHead(node);

        // 因为是共享模式，出队之后则需要判断是否可以唤醒后续等待节点，即后续等待节点是否可以获取锁
        if (propagate > 0 || h == null || h.waitStatus < 0
                // 因为head方法的开头是赋值给了局部变量，可能在执行到此处发生变化了，所以重新读取并赋值
                || (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            // 因为next是null，因此尝试释放，可能存在执行过程，其他线程加入队列进行等待
            // 如果s不是null，则需要判断下个节点是否是共享节点
            if (s == null || s.isShared()) {
                // 尝试释放下个共享节点
                doReleaseShared();
            }
        }
    }

    /**
     * 此方法仅被Acquire方法调用;
     * (出队操作)
     * <p>
     * head的thread和prev都为空，作用相当于出队；
     * 这样做是因为需要找到下个node，即下个线程；本质上就是相当于一个队列的指针，用来指向队列的第一个等待出队的node,
     * 即第一请求同步器的线程；（FIFO）
     *
     * @param node
     */
    private void setHead(Node node) {
        // 因为只有获取锁的线程设置head，而每次操作只有head节点之后的第一个节点操作的，所以不需要CAS
        head = node;
        // 出于GC和减少Traversals
        node.prev = null;
        node.thread = null;
    }

    /**
     * wake up node's successor
     * <p>
     * 唤醒和获取锁是不同的概念；
     * 独占和共享模式共用unparkSuccessor
     *
     * @param node
     */
    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0) {
            compareAndSetWaitStatus(node, ws, 0);
        }

        /**
         * 因为在addWaiter的enq时，CAS tail和t.next = node不是一个原子操作，
         * 则有可能一个线程在addWaiter入列，另一个在unparkSuccessor,因此此时如果从head往后找，
         * 则有可能找不到next；但是因为node.prev = t操作是在CAS之前，如果CAS tail失败则入列失败，
         * 则还不在队列中。所以从tail开始往前遍历到当前node，是可以找到successor的。
         */

        Node s = node.next;
        // upark 下个waitStatus小于等于0
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) {
                // 跳过CANCELLED的线程;此处状态要小于等于0，因为有可能是新增的node，状态是0；
                if (t.waitStatus <= 0) {
                    s = t;
                }
            }
        }

        if (s != null)
            LockSupport.unpark(s.thread);
    }

    /**
     * 试图以排他模式获取对象,如果被中断则异常中止,如果超时则失败;
     * 实现时需要优先检查中断状态,然后至少调用一次tryAcquire()方法,在成功时返回。
     * 否则,在成功之前,线程被中断,或者超时之前,将当前线程加入队列,线程可能重复被阻塞或者非阻塞,调用
     * tryAcquire()方法。这个方法用于实现Lock.tryLock(long,TimeUtil)方法。
     *
     * @param agr          acquire参数,会被传递到tryAcquire()方法,可以表示任何内容
     * @param nanosTimeOut 等待的最大毫秒数;
     * @return true:如果获取成功;false:如果超时;
     * @throws InterruptedException 线程中断异常
     */
    public final boolean tryAcquireNanos(int agr, long nanosTimeOut) throws InterruptedException {
        // 判断当前线程的中断状态
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        // 先判断当前是否可以直接获取到,如果不符合条件则指定时间等待获取锁
        return tryAcquire(agr) || doAcquireNanos(agr, nanosTimeOut);
    }

    /**
     * 试图在独占模式下获取对象。此方法的实现需要检查对象的状态是否允许在独占模式下被获取;如果可以则获取他;
     * <p>
     * 此方法总是由执行acquire方法的线程调用。如果此方法报告失败,则acquire方法可以将当前线程加入队列(如果当前
     * 线程还没有加入队列)直到获取到其他线程释放该线程的信号。此方法可以用来实现Lock.tryLock方法。
     * <p>
     * 此方法必须被子类实现,默认实现将抛出UnsupportedOperationException;
     *
     * @param agr Acquire参数,该值总是传递给 acquire 方法的那个值，
     *            或者是因某个条件等待而保存在条目上的值。该值是不间断的，并且可以表示任何内容。
     * @return 如果成功，则返回 true。在成功的时候，此对象已经被获取。
     * @throws IllegalMonitorStateException  - 如果正在进行的获取操作将同步器设置为非法状态。
     *                                       必须以一致的方式抛出此异常(????????)，以便同步正确运行。
     * @throws UnsupportedOperationException - 如果不支持独占模式
     */
    protected boolean tryAcquire(int agr) {
        throw new UnsupportedOperationException();
    }

    /**
     * 检查当前线程之前是否已经存在等待获取同步器的线程。
     * 这个方法的实现等同于(但是可能更高的效率比如下的方式):
     * getFirstQueuedThread != Thread.currentThread() && hasQueuedThreads()
     * =======队列的第一个线程不等于当前线程,但是这个队列有可能是空的
     * <p>
     * 此方法返回true(在当前线程之前存在等待线程)无法保证其他线程一定在当前线程之前获取,因为可能因为
     * 中断被cancel,或者超时的发生。Likewise, it is possible for another thread to win a
     * race to enqueue after this method has returned {@code false},
     * due to the queue being empty.(????????)
     * <p>
     * 这个方法被设计用于公平同步器,避免barging;如果此方法返回true,则公平同步器的tryAcquire()返回
     * false,tryAcquireShared应该返回负数(除非是reentrant acquire);比如一个公平的可重入的排他的
     * 同步器的的tryAcquire()应该是这样的:
     * protected boolean tryAcquire(int arg){
     * if (isHeldExclusively()) {
     * // A reentrant acquire; increment hold count
     * return true;
     * } else if (hasQueuedPredecessors()) {
     * return false;
     * } else {
     * // try to acquire normally
     * }
     * }
     *
     * @return true:如果在当前线程之前存在排队的线程;false:队列是空的或者当前是队列第一个node;
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.(?????)
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        // ???????实现原理
        return h != t &&
                ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    /**
     * 是否有Sync的线程;
     * 因为并发并行的原因，结果不确定
     *
     * @return
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 因为并发并行的原因，结果不确定;
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *                                      is not held
     * @throws IllegalArgumentException     不是当前Sync的Condition
     * @throws NullPointerException         if the condition is null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * 这个方法用在读写锁中，判断head节点的下个节点是否正在申请独占锁;
     *
     * @return
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
                (s = h.next) != null &&
                !s.isShared() &&
                s.thread != null;
    }

    /**
     * Given Condition是否是当前Sync所拥有
     *
     * @param condition
     * @return
     * @throws NullPointerException
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }


    /**
     * 在排他计时模式获取
     *
     * @param arg          acquire参数
     * @param nanosTimeout 最大等待时间
     * @return true:如果获取到;
     * @throws InterruptedException
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        // 因为在调用doAcquireNanos之前先调用tryAcquire方法,而tryAcquire方法不能立即获取会再调用
        // 此方法,所以如果同时等待时间为0,则立即返回false.
        if (nanosTimeout <= 0L) {
            return false;
        }

        Node node = addWaiter(Node.EXCLUSIVE);
        long deadline = System.nanoTime() + nanosTimeout;
        boolean failed = true;

        try {
            for (; ; ) {
                Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    failed = false;
                    return true;
                }

                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout < 0)
                    return false;
                // 如果超时时间小于阀值，则不通过挂起线程，而是通过自旋的方式
                if (shouldParkAfterFailedAcquire(p, node)
                        && nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    //------------------------releases
    /**
     * release的方法调用时，有可能队列没有任何node。
     * 释放锁其实是两步：1唤醒下个节点2被唤醒的节点获取锁；
     * release是唤醒的动作，有可能就没有线程被挂起的情况
     */

    /**
     * Releases in exclusive mode. Implemented by unblocking one or more thread
     * if tryRelease return true.
     *
     * @param arg
     * @return {@link #tryRelease}的结果
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0) {
                unparkSuccessor(h);
            }
            return true;
        }
        // fail
        return false;
    }

    /**
     * 共享锁释放
     *
     * @param arg
     * @return
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    /**
     * 因为AQS通过维护整型的state来实现锁的获取释放；
     * 因此释放锁本质是改变state；
     * <p>
     * <p>This method is always invoked by the thread performing release.
     * <p>
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *            passed to a release method, or the current state value upon
     *            entry to a condition wait.  The value is otherwise
     *            uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     * waiting acquire (shared or exclusive) to succeed; and
     * {@code false} otherwise
     * @throws IllegalMonitorStateException  if releasing would place this
     *                                       synchronizer in an illegal state. This exception must be
     *                                       thrown in a consistent fashion for synchronization to work
     *                                       correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set a state to reflect a release in exclusive mode.
     * The default implementation throws UnsupportedOperationException.
     *
     * @param arg
     * @return
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 释放：只是唤醒下个节点，然后节点自己尝试获取锁；
     * <p>
     * 需要注意区分 调用（唤醒）线程和工作线程
     */
    protected void doReleaseShared() {
        for (; ; ) {
            Node h = head;
            // 因为release方法会在每次释放锁的时候调用，而调用的时候可能队列没有等候的线程
            if (h != null && h != tail) {
                int ws = head.waitStatus;
                /**
                 * 下个节点如果是挂起状态
                 */
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) {
                        // 如果cas失败则需要再次loop
                        continue;
                    }
                    unparkSuccessor(h);
                    // 此处使用常量，使代码的可读性更好
                    // 如果下面的if执行成功，则是不会出现死锁的，因为线程挂起要求，前置节点的
                    // waitStatues必须是Node.SIGNAL
                } else if (ws == 0
                        && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)) {
                    // 如果head节点的ws==0，说明还没有后续节点在等待;所以需要将head节点设置为
                    // 下个线程可以唤醒。因为是共享模式，所以设置为PROPAGATE
                    continue;
                }
                if (h == head) {
                    // 在进行这些操作时，可能其他线程并发发起对队列的操作，所以需要进行判断是否发生改变；
                    break;
                }
            }
        }
    }

    /**
     * 释放锁，同时返回释放前的状态；
     * 如果释放失败抛出监视器错误异常，同时当前节点CANCELLED;
     *
     * @param node
     * @return
     * @throws IllegalMonitorStateException 是一个 {@link RuntimeException}
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    /**
     * 获取Sync队列上所有获取互斥获取锁的线程
     *
     * @return
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<>();
        // 因为先设置pre，后设置next，之间存在时间差，所以逆序遍历
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * 获取第一个节点的（等待时间最长的）线程；
     * 如果队列为空返回null；
     *
     * @return null 如果队列是空
     */
    public final Thread getFirstQueuedThread() {
        return head == tail ? null : fullGetFirstQueuedThread();
    }

    /**
     * 获取Sync队列上的线程；
     * 因为并发/并行的原因，结果存在不确定性
     * <p>
     * 个人理解：
     * 问题： 为什么不直接使用list.add(p.thread)的方式？
     * 答：因为即使先做判断p.thread != null，但是在执行list.add(p.thread)是存在执行过程，是花费执行时间的，
     * 存在并发和并行的可能，所以无法保证list中的元素不存在null的可能；
     *
     * @return
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            // 因为获取线程，所以线程为判断焦点；获取什么，就以什么为判断焦点
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 因为并发或者并行的原因，结果是不确定的
     *
     * @return
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * 获取Condition队列的等待线程;
     * 因为并发并行的原因，结果是不确定的
     *
     * @param condition
     * @return
     * @throws IllegalArgumentException 非当前同步器的Condition
     */
    public final Collection<Thread> getWaitingThread(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * 是否存在竞争获取锁；是否有线程加入过队列,包括曾经的加入;
     *
     * @return
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 判断Node是否是在Sync Queue；
     *
     * @return
     */
    final boolean isOnSyncQueue(Node node) {
        // 如果状态是Condition或者prev为null，则肯定不在Sync Queue中
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        // 因为SyncQueue通过是反向遍历的方式，优先设置prev节点，所以next不为空，一定是已经成功入队
        if (node.next != null)
            return true;
        // 从Sync Queue的tail开始反向查找指定的节点
        return findNodeFromTail(node);
    }

    /**
     * Returns true if the given thread is currently queued.
     * <p>
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * @throws NullPointerException if the thread is null
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (; ; ) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * 获取第一个等待的线程
     *
     * @return
     */
    private Thread fullGetFirstQueuedThread() {
        /**
         * 需要考虑并发的场景，因为其他线程可能在并发的修改Sync队列;
         * 因为CPU是多核并且是线程切换轮流占有CPU执行的
         */
        Node h, s;
        Thread st;
        // 因为并发/并行的场景,所以执行两遍同样的规则
        if (((h = head) != null && (s = h.next) != null
                && s.prev == head && (st = s.thread) != null)
                || ((h = head) != null && (s = h.next) != null
                && s.prev == head && (st = s.thread) != null))
            return st;

        // 如果快速获取失败，则遍历整个Sync队列
        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = t.thread;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * 是否被当前线程(调用此方法的线程)排他持有；由子类实现；
     * 只会被 {@link ConditionObject}调用；因此支持 {@link ConditionObject}才需要实现，否则无须实现
     *
     * @return
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * Node从Condition队列转移至Sync队列
     *
     * @param node
     * @return
     */
    final boolean transferForSignal(Node node) {
        // 改变waitStatus
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            return false;
        }

        Node p = enq(node);
        int ws = p.waitStatus;
        /**
         * 因为新增Node，所以前个节点需要唤醒后续节点；
         * 为什么需要先判断ws>0？
         * 因为ws>0说明此节点已经被CANCELLED，则不应该将其waitStatus状态设置为SIGNAL；
         */
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)) {
            LockSupport.unpark(node.thread);
        }
        return true;
    }

    /**
     * 线程await过程中，被中断唤醒等（非SIGNAL场景），则将其Node节点从Condition队列转移至Sync队列；
     * 但是需要注意如下场景：
     * 1. 在被中断或者超时等场景下，可能其他线程也发起了SIGNAL
     * 2. 中断和超时都被认为是CANCEL
     *
     * @param node
     * @return true 如果在从Condition队列上Cancel排队成功（非SIGNAL场景），则返回true；
     */
    final boolean transferAfterCancelledWait(Node node) {
        // 当前线程暂时失去CPU，或者其他线程发生并发SIGNAL, 则CAS失败
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        // 在被中断或者超时等场景下，可能其他线程也发起了SIGNAL
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    //-------------------------------------------------  Inner Class

    /**
     * ConditionObject可以序列化，但是其属性是不可以序列化;
     * <p>
     * Condition实现：
     * 1. 线程在await在挂起之后
     * 1.1 通过SIGNAL将Node从Condition队列转移至Sync队列，排队获取锁，再返回;
     * 1.2 通过中断唤醒线程，将Node从Condition队列转移至Sync队列，排队获取锁，再返回；
     * 在使用Condition一定要捕获异常，最后确保锁的释放;
     * <p>
     * 个人理解：
     * 1. 因为Condition的使用是先获取当前Condition关联的Lock，所以不存在并发修改的问题，所以
     * 无须考虑复杂的并发情景，所以可以简化实现；
     * 2. 等待锁的Sync队列和Condition的队列是不同的队列;
     */
    public class ConditionObject implements Condition, Serializable {
        private static final long serialVersionUID = 1173984872572414699L;

        //-------------------------------------------------  Instance Variables
        /**
         * Condition用来表示条件调对象，维护当前条件下的等待队列；
         * 队列的实现通过维护一个Node的链表实现
         */
        private transient Node firstWaiter;
        private transient Node lastWaiter;

        //-------------------------------------------------  Static Variables
        /**
         * 当从await出来时，需要再次interrupt;
         * 因为可能在Condition队列上时没有中断，但是可能在Sync队列上时中断，因此此时只需要重置中断状态
         */
        private static final int REINTERRUPT = 1;
        // 当从await出来时，抛出异常
        private static final int THROW_IE = -1;

        //-------------------------------------------------  Constructor
        public ConditionObject() {
        }

        //-------------------------------------------------  Internal methods
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiter();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            // 默认如果tail节点为空，则没有初始化（通chl队列实现）
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * 在添加新的等待线程时，判断最后一个节点是否被Cancelled，如果Cancelled则直接释放；
         * 因为在添加waiter时进行此动作，因此是在持有锁的情况下进行的操作；
         * <p>
         * 从头开始逐个遍历Node，删除被Cancelled Node;
         * <p>
         */
        private void unlinkCancelledWaiter() {
            // 指向当前遍历的节点
            Node t = this.firstWaiter;
            // 用于指向最后一个node
            Node trail = null;
            // 从头开始遍历
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    // 断开对下个节点的链接
                    t.nextWaiter = null;
                    if (trail == null) {
                        // 没有成功找到第一个CONDITION的NODE
                        this.firstWaiter = next;
                    } else {
                        trail.nextWaiter = next;
                    }
                    // 设置最后一个节点;只有当前节点状态非CONDITION且next为空时，才需要改变last节点，否则不需要
                    if (next == null) {
                        lastWaiter = tail;
                    }
                } else {
                    trail = t;
                }
                t = next;
            }
        }


        /**
         * 这之间涉及转换的过程;
         *
         * @throws InterruptedException
         * @throws IllegalMonitorStateException 错误监视器状态异常,释放锁失败
         */
        @Override
        public void await() throws InterruptedException {
            // 检查中断状态
            if (Thread.interrupted())
                throw new InterruptedException();

            /*加入Condition队列进行等待*/
            Node node = addConditionWaiter();

            // 如果释放锁失败，则当前线程对应的Node会被设置为Cancelled;
            int savedState = fullyRelease(node);

            /**
             * 线程挂起等待
             */
            int interruptMode = 0;
            /**
             * 为什么需要先判断isOnSyncQueue?
             * 1. 在程序执行过程中，各个线程是通过调度，不断获取CPU时间片切换执行（现在多数是多核CPU）；因此在执行到此处时
             * 可能发生其他线程发起SIGNAL；也可能当前线程失去CPU，由其他线程执行；
             */
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }

            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            // 退出时当前节点需要从Condition的队列移除
            if (node.nextWaiter != null)
                unlinkCancelledWaiter();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        private void reportInterruptAfterWait(int interruptMode)
                throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * 如果中断且Cancel成功，则为 THROW_IE;
         * 如果中断但是Cancel失败，则为 REINTERRUPT；
         * 否则为0；
         *
         * @param node
         * @return
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                    (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                    0;
        }

        @Override
        public void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);

            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }

            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        @Override
        public long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            long deadline = System.currentTimeMillis() + nanosTimeout;

            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0) {
                    transferAfterCancelledWait(node);
                    break;
                }

                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);

                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;

                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiter();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * 超时时间是等待的超时时间，并包括获取锁的时间
         *
         * @param time
         * @param unit
         * @return false 如果超时之前没有等待SIGNAL，则返回false
         * @throws InterruptedException
         */
        @Override
        public boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanoTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();

            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);

            final long deadline = System.nanoTime() + nanoTimeout;
            boolean timeout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanoTimeout <= 0) {
                    timeout = transferAfterCancelledWait(node);
                    break;
                }

                // 自旋时间，小于阀值则不断的自旋，降低线程切换带来的时间消耗
                if (nanoTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanoTimeout);

                // 发生中断场景
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;

                nanoTimeout = deadline - System.nanoTime();
            }

            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiter();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timeout;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            long absTime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();

            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);

            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > absTime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(absTime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }

            // 获取锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            // remove node from condition
            if (node.nextWaiter != null)
                unlinkCancelledWaiter();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * 将线程从Condition的队列移动到Lock的队列
         */
        @Override
        public void signal() {
            if (isHeldExclusively())
                throw new IllegalMonitorStateException();
            // 先入先出队列
            Node first = this.firstWaiter;
            if (first != null)
                doSignal(first);
        }

        private void doSignal(Node first) {
            do {
                // 唤醒第一个则后续重置第一个
                if ((firstWaiter = firstWaiter.nextWaiter) == null)
                    lastWaiter = null;
                // 出队的Node next重置为null；
                // 如果原来的fisrtWaiter的next不为空，则重置为空；如果就是null，重置一次null，也没关系
                first.nextWaiter = null;
            } while (!(transferForSignal(first))
                    && (first = firstWaiter) != null);
        }

        private void doSignalAll(Node first) {
            // 因为唤醒所有的等待线程，清空队列
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        @Override
        public void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * 当前ConditionObject的实例归属的 {@link AbstractQueuedSynchronizer}
         *
         * @param sync
         * @return
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            // AbstractQueuedSynchronizer.this 获取当前ConditionObject所属的
            // {@link AbstractQueuedSynchronizer} 实例
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Condition队列上所有等待的线程
         *
         * @return
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                // waitStatus状态
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }

        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Condition队列是否存在等待的线程
         *
         * @return
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }
    }
    //-------------------------------------------------  Static Class

    /**
     * AQS的实现基于一个FIFO的lock queue，Queue的每个元素一个Node或者
     * 可以称为Queue的每个节点Node，都对应一个线程。
     * <p>
     * 等待队列的Node类,阻塞线程队列的节点;等待队列是CHL所队列的变体;
     * <p>
     * CAS只能对指定的内存空间(单个共享变量)保证原子操作,线程安全;
     * 但是多个共享变量是无法保证的,但是可以将多个变为一个,从而达到原子操作,线程安全;
     * <p>
     * 1. 入队只需加入队尾设置tail，出队则设置head
     * 2. 需要处理后继节点的问题，因为后继节点存在被取消或者超时的情况
     * 3. 如果一个节点被Cancel，则需要将后继节点link到一个非空的前继节点
     */
    static final class Node {
        /**
         * 代表节点以共享模式；利用地址比较，提高速度
         */
        static final Node SHARED = new Node();
        /**
         * 代表节点以排他模式,独占模式
         */
        static final Node EXCLUSIVE = null;

        //waitStatus

        /**
         * 代表线程等待被Canceled。node因为超时或者中断被cancelled。
         */
        static final int CANCELLED = 1;

        /**
         * 代表继任者(接替线程,后继线程)线程需要唤醒；
         * 下个等待节点所对应的线程park，需要unpark
         */
        static final int SIGNAL = -1;

        /**
         * 线程正在CONDITION上等待
         */
        static final int CONDITION = -2;

        /**
         * 代表后续节点会传播唤醒动作，共享模式起作用
         */
        static final int PROPAGATE = -3;

        //前置节点
        volatile Node prev;
        //后置节点
        volatile Node next;

        // 拥有当前节点的线程
        volatile Thread thread;

        /**
         * waitStatus的状态只有Cancelled是1，其他状态都是小于0；
         */
        volatile int waitStatus;

        /**
         * nextWaiter用于两个场景：
         * 在CHL队列实现中（非condition的队列时）：
         * 1. nextWaiter用来判断是否共享模式；通过值比较即地址比较的方式快速判断；
         *
         * 在Condition的队列中：
         * 1. 用来指向下个节点,实现链表，从而实现队列
         */
        Node nextWaiter;

        Node() { //用于新建head和shared
        }

        Node(Thread thread, Node mode) { // used by addWaiter
            this.thread = thread;
            this.nextWaiter = mode;
        }

        Node(Thread thread, int waitStatus) { // used by Condition
            // 其实默认当前节点的mode是互斥的，因为nextWaiter不等于SHARED
            this.thread = thread;
            this.waitStatus = waitStatus;
        }

        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null) {
                throw new NullPointerException();
            } else {
                return p;
            }
        }

        final boolean isShared() {
            return nextWaiter == SHARED;
        }
    }

    //-------------------------------------------------  Static Code
    /**
     * Unsafe是JDK提供对操作系统底层的访问,建议只在JDK的类库中使用;
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            //获取Field的偏移量
            stateOffset = unsafe.objectFieldOffset(
                    AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset(
                    AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(
                    AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset(
                    Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }
}
