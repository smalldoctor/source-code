package com.alibaba.dubbo.common.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过借助{@link java.util.concurrent.ConcurrentHashMap}实现线程安全的Set
 *
 * @param <E>
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, Serializable {
    //-------------------------------------------------  Static Variables
    private static final long serialVersionUID = -8672117787651310382L;
    /**
     * Map的key集合实际上就是相当于一个Set，所以只要value是固定值，则转换为Map的实现
     */
    private static final Object PRESENT = new Object();

    //-------------------------------------------------  Instance Variables

    private final ConcurrentHashMap<E, Object> map;

    //-------------------------------------------------  Constructors


    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<>(initialCapacity);
    }

    //-------------------------------------------------  Instance Methods

    /**
     * MAP的key集合是当前的Set
     *
     * @return
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Map的size就是key的集合的size
     *
     * @return
     */
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 此处使用技巧：
     * 判断map的put方法的返回值是否等于null;
     * <p>
     * 一般不以map的put等方法的返回值作为结果的判断标识，因为存在key和value可能为null的场景，所以存在不确定性;
     *
     * @param e
     * @return
     */
    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        // 因为MAP的value是固定值
        return map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
