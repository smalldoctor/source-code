package com.alibaba.dubbo.common.utils;

/**
 * 用于持有value的工具类
 *
 * @param <T>
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
