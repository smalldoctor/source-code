package io.netty.concurrent;

/**
 * 可写的ProgressiveFuture，即ProgressivePromise
 *
 * @param <V>
 */
public interface ProgressivePromise<V> extends Promise<V>, ProgressiveFuture<V> {
}
