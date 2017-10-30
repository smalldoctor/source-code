package java.util.concurrent.locks;

import sun.misc.Unsafe;

/**
 * @Author: xuecy
 * @Date: 2016/10/21
 * @RealUser: Chunyang Xue
 * @Time: 10:41
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */

/**
 * Object的wait和notify也是支持线程的阻塞和唤醒；LockSupport也是支持线程的阻塞和唤醒的，但是与Object的面向的对象不同，
 * 面向的触发者不同；LockSupport直接是以线程作为参数，作为入口，语意更加明确的，清晰的；而Object以某个对象为触发点，线程是
 * 被动的，并且无法准确的控制哪个线程，什么时候阻塞及唤醒。
 * 差异：
 * 1. wait/notify需要获取对象的监视器；LockSupport是不需要的；
 * // 获取监视器
 * wait() // 释放监视器
 * // 再次获取监视器
 * -----------------------------------------
 * Object的wait方法需要放在循环条件中进行调用，对条件进行re-check：主要是因为监视器可能会被多个场景使用，wait方法只是re-acquire监视器
 * 的场景之一；
 * Object的notifyAll方法代替notify使用，以避免deadlock。
 * 参考：https://app.yinxiang.com/shard/s34/nl/7217258/18b64552-6a98-4e68-840c-7f0bf8d293fa/?csrfBusterToken=U%3D6e206a%3AP%3D%2F%3AE%3D15be275e0ce%3AS%3D64bacfb1727e5377e48bbd157951199f
 * -----------------------------------------
 * 2. 两者实现机制不同，没有交集；
 * <p>
 * 线程正确停止的方法：
 * 1. 共享变量
 * 1.1 通过设置共享变量，然后周期性的遍历这个共享变量；但是如果线程处于阻塞状态是无法检查共享变量的；
 * 2. Thread.interrupt()
 * 2.1 interrupt本质是无法中断正在运行的线程的，只能中断处于阻塞状态的线程；
 * 2.2 在线程阻塞状态被中断之后，再次周期性的检查共享变量
 * <p>
 * LockSupport.park也是支持中断的，但是不会抛出InterruptedException;如果需要check线程是被unpark还是interrupt的，
 * 可以通过check线程的interrupted status，即通过调用Thread.interrupted或者Thread.isInterrupted方法；Thread.interrupted
 * 方法会clear interrupted status，但是isInterrupted不会clear。
 */
public class LockSupport {
    /**
     * 不允许实例化
     */
    private LockSupport() {
    }

    private static void setBlocker(Thread t, Object arg) {
        // parkBlocker用于在线程被blocked时记录被谁blocker,用于线程监控和分析定位原因
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * Unpark指定的线程；
     *
     * @param thread 如果是null，则不做任何处理
     */
    public static void unpark(Thread thread) {
        if (thread != null) {
            UNSAFE.unpark(thread);
        }
    }

    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(false, 0L);
        setBlocker(t, null);
    }

    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    public static Object getBlocker(Thread thread) {
        if (thread == null) {
            throw new NullPointerException();
        }
        return UNSAFE.getObjectVolatile(thread, parkBlockerOffset);
    }

    public static void park() {
        UNSAFE.park(false, 0);
    }

    public static void parkNanos(long nanos) {
        if (nanos > 0) {
            UNSAFE.park(false, nanos);
        }
    }

    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        } else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0) {
            r = 1;
        }
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    private final static Unsafe UNSAFE;
    private final static long parkBlockerOffset;
    private final static long SECONDARY;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class tk = Thread.class;
            parkBlockerOffset = UNSAFE.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
            SECONDARY = UNSAFE.objectFieldOffset(tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
