package java.util.concurrent;

import com.sun.xml.internal.bind.v2.TODO;

import java.util.concurrent.locks.LockSupport;

/**
 * 因为 {@code FutureTask}是实现 {@link Future}，所以是一个可以跟踪且维护的异步任务；
 * 当任务没有执行完成时，get方法会阻塞；
 * 已经完成的任务（包括正常结束，异常结束，通过cancel取消结束的
 * ）不可以取消或者重启；但是可以通过runAndReset的方法重启；
 * <p>
 * {@code done}用来进行task {@code isDone}之后的相关工作，如回调等；
 *
 * @param <V>
 */
public class FutureTask<V> implements RunnableFuture<V> {
    //-------------------------------------------------  Instance Variables
    /**
     * task初始化状态为New，通过set，setException和cancel状态转换为
     * 结束状态；
     * During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)).
     * <p>
     * 可能的状态变化过程：
     * NEW->COMPLETING->NORMAL
     * NEW->COMPLETING->EXCEPTIONAL
     * NEW->CANCELLED
     * NEW->INTERRUPTING->INTERRUPTED
     */
    private volatile int state;

    // FutureTask封装的Callable；Runnable的实现需要转换为Callable
    private Callable<V> callable;

    // 通过get方法获取的结果，或者抛出的异常
    // outCome非volatile类型，因为通过状态判断获取
    private Object outCome;
    //运行当前Callable的线程
    private volatile Thread runner;
    //
    private volatile WaitNode waiters;
    //-------------------------------------------------  Static Variables
    private static final int NEW = 0;
    // 调用set方法设置完成结果，所以相当于任务已经完成
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    /**
     * INTERRUPTING和INTERRUPTED状态是通过cancel(true)获得的；
     * 因此也属于取消状态；
     */
    private static final int CANCELLED = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED = 6;

    //-------------------------------------------------  Constructs

    /**
     * 初始化状态为NEW
     *
     * @param runnable
     * @param result   可以没有返回值，可以是null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;
    }

    //-------------------------------------------------  Instances Methods

    /**
     * 返回Result或者抛出异常
     *
     * @param s 状态
     * @return
     * @throws ExecutionException
     */
    private V report(int s) throws ExecutionException {
        Object x = outCome;
        if (s == NORMAL) {
            return (V) x;
        }
        if (s >= CANCELLED) {
            throw new CancellationException();
        }
        /**
         * 如果是状态是NEW时，那么outCome是null，则不存在类型转换的异常
         */
        throw new ExecutionException((Throwable) x);
    }

    @Override
    public void run() {

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 除了NEW状态之外，其他状态都已经是或者即将完成的状态，所以不可以取消；
        if (!(state == NEW
                && UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) {
            return false;
        }
        try {
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null) {
                        t.interrupt();
                    }
                } finally {
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * Unpark挂在当前Task上的thread
     */
    private void finishCompletion() {
        for (WaitNode q; (q = waiters) != null; ) {
            // CAS交换保证线程安全,即保证取到最新的值
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (; ; ) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null) {
                        break;
                    }
                    q.next = null;
                    q = next;
                }
                break;
            }
        }

        // 进行任务完成后的事情
        done();

        // 完成之后，Callable置空
        callable = null;
    }

    /**
     * 如果task的状态为 {@code isDone}，则调用 {@code done}进行
     * 后续的处理，比如进行回调等；
     */
    protected void done() {
    }

    @Override
    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    /**
     * isDone任务是否已经被处理，只要不是NEW状态，则都说明已经被处理；
     *
     * @return
     */
    @Override
    public boolean isDone() {
        return state != NEW;
    }

    /**
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws CancellationException 运行时异常
     */
    @Override
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING) {
            s = awaitDone(false, 0L);
        }
        return report(s);
    }

    /**
     * @param timed 是否计时；超时
     * @return
     * @throws InterruptedException
     */
    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = timed ? System.currentTimeMillis() + nanos : 0L;
        // 代表当前线程
        WaitNode q = null;
        boolean queued = false;
        for (; ; ) {
            /**
             * {@link LockSupport#park()}在阻塞被中断时是不会抛出异常的；
             * 因此需要自己检查异常
             * 因为task尚未完成，因此当前线程在阻塞等待完成
             */
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }
            int s = state;
            if (s > COMPLETING) {
                // task已经完成
            } else if (s == COMPLETING) {
                // task正在进行赋值处理
            } else if (q == null) {
                q = new WaitNode();
            } else if (!queued) {
                // 将当前线程存放在队列的头
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
            } else if (timed) {
                nanos = deadline - System.currentTimeMillis();
                if (nanos <= 0) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            } else {
                LockSupport.park(this);
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            /**
             * 移除算法：
             * 通过将代表当前线程的WaitNode的thread置为空，然后
             * 遍历waiters,将所有thread == null的WaitNode从栈中删除
             */
            retry:
            for (; ; ) {
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    // 后继节点
                    s = q.next;
                    if (q.thread != null) {
                        pred = q;
                    } else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) { // removeWaiter race
                            continue retry;
                        }
                    } else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s)) {
                        // 第一个节点可能是空的
                        continue retry;
                    }
                }
                break;
            }
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    //-------------------------------------------------  Static Classes

    /**
     * 因为内部使用的类，禁止Override
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            this.thread = Thread.currentThread();
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
