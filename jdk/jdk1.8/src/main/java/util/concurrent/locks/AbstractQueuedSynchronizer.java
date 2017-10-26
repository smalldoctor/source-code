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
     * 至少调用tryAcquire一次，如果tryAcquire返回失败，则线程将被放入队列
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
     * 线程会被反复的唤醒或者阻塞，直到tryAcquire成功;
     * 独占模式，忽视中断
     *
     * @param node
     * @param arg
     * @return
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
                // p != head 或者 tryAcquire失败，则挂起线程；
                // 前继节点waitStatus为SINGAL（后继节点需要唤醒）
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
     * 共享模式
     *
     * @param arg
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0) {
            doAcquireShared(arg);
        }
    }

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
                            // 因为park是支持中断，但是不会抛出异常，所以检查是否中断，如果是则重置中断状态
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

    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
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
     * 检查线程在获取锁失败之后，是否需要被block
     *
     * @param pred
     * @param node
     * @return
     */
    private static final boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL) {
            // 因为前继节点的状态已经SIGNAL,asking a release to signal it
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
            // CONDITION状态用于ConditionObject,不会出现在此处;
            // waitStatus是0(初始化)或者PROPAGATE；
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        // 如果前继节点没有waitStatus=SIGNAL，则说明当前线程没有阻塞，则再尝试获取一次锁
        return false;
    }

    /**
     * AQS的实现基于一个FIFO的lock queue，Queue的每个元素一个Node或者
     * 可以称为Queue的每个节点Node，都对应一个线程。
     * <p>
     * 等待队列的Node类,阻塞线程队列的节点;等待队列是CHL所队列的变体;
     * <p>
     * CAS只能对指定的内存空间(单个共享变量)保证原子操作,线程安全;
     * 但是多个共享变量是无法保证的,但是可以将多个变为一个,从而达到原子操作,线程安全;
     */
    static final class Node {
        /**
         * 代表节点以共享模式
         */
        static final Node SHARED = new Node();
        /**
         * 代表节点以排他模式,独占模式
         */
        static final Node EXCLUSIVE = null;

        //waitStatus

        /**
         * 代表线程被Canceled。node因为超时或者中断被cancelled。
         */
        static final int CANCELLED = 1;

        /**
         * 代表继任者(接替线程,后继线程)线程需要唤醒,即结束阻塞状态
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

        volatile int waitStatus;

        /**
         *
         */
        Node nextWaiter;

        Node() { //用于新建head和shared
        }

        Node(Thread thread, Node mode) { // used by addWaiter
            this.thread = thread;
            this.nextWaiter = mode;
        }

        Node(Thread thread, int waitStatus) { // used by Condition
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

    /**
     * 等待队列的头,懒初始化(队列新建时是null)。除了初始化,只能使用setHead方法进行修改。
     * 如果是队列的头,需要保证它的waitState不能是Cancelled。
     * <p>
     * volatile 多线程
     */
    private transient volatile Node head;

    /**
     * 等待队列的tail node,懒初始化(队列新建时是null),只能通过enq追加。
     */
    private transient volatile Node tail;

    /**
     * Synchronizer的state
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

        // 此处条件无法理解。。。？
        if (propagate > 0 || h == null ||
                h.waitStatus < 0) {
            Node s = node.next;
            // 因为next是null，因此不支持next的情况，因此尝试释放
            // 如果s不是null，则需要判断下个节点是否是共享节点
            if (s == null || s.isShared()) {
                // 尝试释放下个共享节点
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
        if (nanosTimeout < 0) {
            return false;
        }
        // 计算出等待的结束时间
        final long deadline = System.nanoTime() + nanosTimeout;
        return false;
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
     * @return
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
     * Attempts to set a state to reflect a release in exclusive mode.
     * The default implementation throws UnsupportedOperationException.
     *
     * @param arg
     * @return
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    protected void doReleaseShared() {
        for (; ; ) {
            Node h = head;
            // 因为release方法会在每次释放锁的时候调用，而调用的时候可能队列没有等候的线程
            if (h != null && h != tail) {
                int ws = head.waitStatus;
                // 因为存在挂起的线程，那么挂起线程的前置节点的waitStatus是Node.SIGNAL，
                // 只有SIGNAL时，才会挂起线程。
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
                    // 因为获取同步器的线程会出列，而出列则是将head置为获取到的同步器的线程归属节点
                    // 如果h==head说明没有变化，则推出循环
                    break;
                }
            }
        }
    }
}
