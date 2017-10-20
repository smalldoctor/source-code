package io.netty.concurrent;

/**
 * 异步的调度的操作的结果
 *
 * @param <V>
 */
public interface ScheduledFuture<V> extends Future<V>, java.util.concurrent.ScheduledFuture<V> {
}
