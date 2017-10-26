package java.util.concurrent;

import java.util.Random;

/**
 * @Author: xuecy
 * @Date: 2017/3/12
 * @RealUser: Chunyang Xue
 * @Time: 08:15
 * @Package: rmxue.java.util.concurrent
 * @Email: 15312408287@163.com
 */
public class ThreadLocalRandom extends Random {
    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }


    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;

    private static final long PROBE;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            PROBE = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomProbe"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
