package rmxue.java.util.concurrent;

import sun.misc.Contended;
import sun.misc.Unsafe;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: xuecy
 * @Date: 2017/3/4
 * @RealUser: Chunyang Xue
 * @Time: 20:14
 * @Package: rmxue.java.util.concurrent
 * @Email: 15312408287@163.com
 */
public class ConcurrentHashMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = -1L;

    /***********************
     * 常量
     ************************/

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final int DEFAULT_CAPACITY = 16;

    private static final int MIN_TRANSFER_STRIDE = 16;

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /*用于Node hash*/
    static final int HASH_BITS = 0x7fffffff;// 32个1


    /*----------------Fields---------------------*/
    /*用于table init和resize的控制;如果是负数,则说明table在init或者resize。
      如果非负数,则在table是null,是需要创建的table的大小或者是0;在初始化之后
      是触发resize的阀值。
     */
    private transient volatile int sizeCtl;

    transient volatile Node<K, V>[] table;

    private transient volatile CounterCell[] counterCells;

    private transient volatile long baseCount;

    /*next table 用于扩容时使用*/
    private transient volatile Node<K, V>[] nextTable;

    /***************
     * static
     ******************/
    /**
     * 用给定的size为基础计算出一个2的N次方的新size(大于等于c的2的最小自然数幂)
     * See Hackers Delight, sec 3.2
     */
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    public static int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }


    //--------------------------访问table元素
    static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i) {
        return (Node<K, V>) UNSAFE.getObjectVolatile(tab, ((long) i << ASHIFT) + ABASE);
    }

    static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i,
                                         Node<K, V> c, Node<K, V> v) {
        return UNSAFE.compareAndSwapObject(tab, ((long) i << ASHIFT) + ABASE, c, v);
    }

    static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v) {
        UNSAFE.putObjectVolatile(tab, ((long) i << ASHIFT) + ABASE, v);
    }

    /**
     * Nodes casTabAt
     **/
    /*
    * key,value被封装在Node;每个Node实例是链表的一个节点。
    * */
    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            return null;
        }
    }

    static final class TreeNode<K, V> extends Node<K, V> {
        public TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }
    }

    static final class TreeBin<K, V> extends Node<K, V> {
        public TreeBin(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }
    }

    /*-------------Count Support-------------------------*/
    @Contended
    static final class CounterCell {
        volatile long value;

        CounterCell(long x) {
            value = x;
        }
    }

    /***************Public Operation******************/
    /**
     * 创建一个新的空的默认table size是16的Map
     */
    public ConcurrentHashMap() {
    }

    /**
     * Creates a new, empty map with an initial table size
     * accommodating the specified number of elements without the need
     * to dynamically resize.
     *
     * @param initialCapacity The implementation performs internal
     *                        sizing to accommodate this many elements.
     * @throws IllegalArgumentException 如果初始化的容量是负数则抛出IllegalArgumentException
     */
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        int cap = (initialCapacity >= MAXIMUM_CAPACITY >>> 1) ? MAXIMUM_CAPACITY :
                tableSizeFor(initialCapacity);
        sizeCtl = cap;
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    /**
     * Creates a new, empty map with an initial table size based on
     * the given number of elements ({@code initialCapacity}), table
     * density ({@code loadFactor}), and number of concurrently
     * updating threads ({@code concurrencyLevel}).
     *
     * @param initialCapacity  the initial capacity. The implementation
     *                         performs internal sizing to accommodate this many elements,
     *                         given the specified load factor.
     * @param loadFactor       the load factor (table density) for
     *                         establishing the initial table size
     * @param concurrencyLevel 并发线程的数量
     * @throws IllegalArgumentException if the initial capacity is
     *                                  negative or the load factor or concurrencyLevel are
     *                                  nonpositive
     */
    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0) {
            throw new IllegalArgumentException();
        }
        if (initialCapacity < concurrencyLevel) {
            initialCapacity = concurrencyLevel;
        }
        long size = (long) (1.0 + (long) initialCapacity / loadFactor);
        int cap = (size >= (long) MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int) size);
        this.sizeCtl = cap;
    }

    /**
     * 在链表中映射key和value;
     * key和value都不能是null
     * <p>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return 返回key映射的旧值, 或者Null
     * @throws NullPointerException if the specified key or value is null
     */
    public V put(K key, V value) {
        return null;
    }

    public void putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            // n是table的length
            int n, i, fh;
            Node<K, V> f;
            if (tab == null || (n = tab.length) == 0) {
                // 初始化
                tab = initTable();
            } else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 空的
                if (casTabAt(tab, i, null, new Node<K, V>(hash, key, value, null))) {
                    break;
                }
            } else if ((fh = f.hash) == -1) {
                // 扩容进行中
            } else {
                // 索引所在处存在元素
                V oldVal = null;
                synchronized (f) {
                    // 针对并发场景,需要double check索引处的Node是否是同一个Node
                    if (tabAt(tab, i) == f) {
                        // Node的hash值不一样,代表的的含义不一样
                        if (fh >= 0) {
                            // 没有发生扩容或者移动,是普通的bin
                            binCount = 1;
                            for (Node<K, V> e = f; ; ++binCount) {
                                // 循环bin
                                K ek;
                                // 如果存在key
                                if (e.hash == hash &&
                                        (((ek = e.key) == key)
                                                || (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent) {
                                        e.val = value;
                                    }
                                    break;
                                }
                                // 如果不存在key
                                Node<K, V> pred = e;
                                // 如果e.next不为空,则继续比较e.next,始终保持e是最新,从而是循环继续
                                if ((e = e.next) == null) {
                                    pred.next =
                                            new Node<K, V>(hash, key, value, null);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {

                        }
                    }
                }
            }
        }
    }

    /**
     * 扩容关键方法
     *
     * @param tab
     * @param nextTab
     */
    private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab) {
        int n = tab.length, stride;
        // 将table分段,然后用于并发扩容
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE) {
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return null;
    }

    // table初始化和resize--------------------
    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        int sc;
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0) {
                Thread.yield();
            } else if (UNSAFE.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                // 需要double check,防止并发重复初始化
                // 对指向volatile定义的变量,在赋值其他变量之后,需要使用时再次赋值,也保持最新
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        Node<K, V>[] nt = (Node<K, V>[]) new Node<?, ?>[n];
                        table = tab = nt;
                        // 相当于0.75n,设置一个扩容阀值
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }

    private final void addCount(long x, int check) {
        CounterCell[] as;
        long b, s;
        if ((as = counterCells) != null ||
                !UNSAFE.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            int m;
            long v;
            CounterCell a;
            boolean uncontended = true;
//            if (as == null || (m = as.length - 1) < 0
//                    || ((a = as[ThreadLocalRandom.getProbe() & m]) == null)
//                    || !(uncontended = UNSAFE.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
//
//            }
        }
    }

    private static final Unsafe UNSAFE;
    private static final long SIZECTL;
    private static final long ABASE;
    private static final long ASHIFT;
    private static final long BASECOUNT;
    private static final long CELLVALUE;

    static {
        try {
            UNSAFE = Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = UNSAFE.objectFieldOffset(k.getDeclaredField("sizeCtl"));
            BASECOUNT = UNSAFE.objectFieldOffset(k.getDeclaredField("baseCount"));
            Class<?> ck = CounterCell.class;
            CELLVALUE = UNSAFE.objectFieldOffset(ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
            // 获取数组对象头的大小,也就是第一个元素的偏移量
            ABASE = UNSAFE.arrayBaseOffset(ak);
            // 获取每个元素的大小
            int scale = UNSAFE.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0) {
                throw new Error("data type scale not a power of two");
            }
            // 用于后面计算元素偏移量使用,通过位运算计算偏移量
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (NoSuchFieldException e) {
            throw new Error();
        }
    }
}
