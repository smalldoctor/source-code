package io.netty.concurrent;

/**
 * 通过指定类型，从而隐藏泛型的便捷接口;（此方式一般用于针对指定场景,知道类型且限定类型的场景）
 *
 * @param <V>
 */
public interface FutureListener<V> extends GenericFutureListener<Future<V>> {
}
