package io.netty.concurrent;

/**
 * 用来持有异步操作的结果；Netty自己实现了自己的Future
 *
 * @param <V>
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
}
