package io.netty.concurrent;

/**
 * 自定义Future的基础抽象实现；
 * 不允许Cancellation
 *
 * @param <V>
 */
public abstract class AbstractFuture<V> implements Future<V> {
}
