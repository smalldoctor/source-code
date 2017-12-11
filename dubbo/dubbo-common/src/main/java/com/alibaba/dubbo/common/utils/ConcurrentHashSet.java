package com.alibaba.dubbo.common.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, Serializable {
    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
