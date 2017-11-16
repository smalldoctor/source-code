package com.alibaba.dubbo.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用 {@link LinkedHashMap}在put和get时进行顺序调整的特性，实现LRU的容器,
 * 即缓存
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    //-------------------------------------------------  Static Variables
    private static final long serialVersionUID = -5167631809472116969L;

    /**
     * 容器的默认最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = 1000;

    /**
     * 容器的默认增长因子，扩容使用
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    //-------------------------------------------------  Instance Variables
    /**
     * 1. 缓存是公用的，所以存在并发的问题，需要使用锁保证线程安全
     * 2. 因为缓存是多个，所以每个缓存需要有自己的锁
     */
    private final Lock lock = new ReentrantLock();

    private volatile int maxCapacity;

    //-------------------------------------------------  Constructors
    public LRUCache() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public LRUCache(int maxCapacity) {
        super(16, DEFAULT_LOAD_FACTOR, true);
        this.maxCapacity = maxCapacity;
    }


    //-------------------------------------------------  Instance Methods

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * 因为 {@link LinkedHashMap}会进行mapping的的删除，所以需要识别何时进行移除；
     * LRUCache是在容量达到最大时进行移除;
     *
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    @Override
    public V get(Object key) {
        try {
            lock.lock();
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            lock.lock();
            return super.containsKey(key);
        } finally {
            lock.lock();
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            lock.lock();
            return super.put(key, value);
        } finally {
            // 确保锁被释放
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return super.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        try {
            lock.lock();
            return super.remove(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            super.clear();
        } finally {
            lock.unlock();
        }
    }
}
