package io.netty.concurrent;

/**
 * Future是只读的；Promise扩展于Future，是可写的Future
 */
public interface Promise<V> extends Future<V> {

}
