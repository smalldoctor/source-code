package java.util.concurrent.atomic;

import sun.misc.Contended;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongBinaryOperator;

/**
 * @Author: xuecy
 * @Date: 2017/3/15
 * @RealUser: Chunyang Xue
 * @Time: 20:06
 * @Package: rmxue.java.util.concurrent.atomic
 * @Email: 15312408287@163.com
 */
public class Striped64 extends Number {
    @Contended
    static final class Cell {
        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("value"));
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }

        volatile long value;

        Cell(long x) {
            value = x;
        }

        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }
    }

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    transient volatile long base;

    transient volatile int cellsBusy;

    transient volatile Cell[] cells;

    Striped64() {

    }

    final boolean casBase(long cap, long val) {
        return UNSAFE.compareAndSwapLong(this, BASE, cap, val);
    }


    final boolean casCellsBusy() {
        return UNSAFE.compareAndSwapLong(this, CELLSBUSY, 0, 1);
    }

    static final int getProbe() {
        return UNSAFE.getInt(Thread.currentThread(), PROBE);
    }

    static final int advanceProbe(int probe) {
        probe ^= probe << 13;
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    final boolean caseBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, BASE, cmp, val);
    }

    /**
     * @param x
     * @param fn
     * @param wasUncontended
     */
    final void longAccumulate(long x, LongBinaryOperator fn, boolean wasUncontended) {
        int h;
        if ((h = getProbe()) == 0) {
            ThreadLocalRandom.current();
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;// 是否发生碰撞,如果true,说明table中对应cell非空,则说明发生碰撞
        for (; ; ) {
            Cell[] as;
            int n;
            Cell a;
            long v;
            if ((as = cells) != null && (n = cells.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {
                        Cell r = new Cell(x);
                        if (cellsBusy == 0 && casCellsBusy()) {
                            boolean created = false;
                            try {
                                Cell[] rs;
                                int m, j;
                                if ((rs = cells) != null
                                        && (m = rs.length) > 0
                                        && rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created) {
                                break;
                            }
                            continue;
                        }
                    }
                    collide = false;
                } else if (!wasUncontended) {
                    wasUncontended = true;
                } else if (a.cas((v = a.value), (fn == null) ? v + x : fn.applyAsLong(v, x))) {
                    break;
                } else if (n > NCPU || cells != as) {
                    collide = false;
                } else if (!collide) {
                    collide = true;
                } else if (cellsBusy == 0 && casCellsBusy()) {
                    try {
                        // 判断是否已经发生变化,as是否已经过时
                        if (cells == as) {
                            //cell table扩容;增加一倍
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; i++) {
                                rs[i] = as[i];
                            }
                            cells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;
                }
                h = advanceProbe(h);
            } else if (cellsBusy == 0 && cells == as && casCellsBusy()) {
                boolean init = false;
                try {
                    if (cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(x);
                        cells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init) {
                    break;
                }
            } else if (caseBase(v = base, (fn == null) ? v + x : fn.applyAsLong(v, x))) {
                break;
            }
        }
    }

    private static final sun.misc.Unsafe UNSAFE;
    private static final long BASE;
    private static final long CELLSBUSY;
    private static final long PROBE;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class ak = Striped64.class;
            BASE = UNSAFE.objectFieldOffset(ak.getDeclaredField("base"));
            CELLSBUSY = UNSAFE.objectFieldOffset(ak.getDeclaredField("cellsBusy"));
            Class tk = Thread.class;
            PROBE = UNSAFE.objectFieldOffset(tk.getDeclaredField("threadLocalRandomProbe"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }
}
