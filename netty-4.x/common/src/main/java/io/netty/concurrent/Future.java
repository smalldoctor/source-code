package io.netty.concurrent;

/**
 * 用来持有异步操作的结果；Netty自己实现了自己的Future；
 * Future间接的代表异步操作
 *
 * @param <V>
 */
public interface Future<V> extends java.util.concurrent.Future<V> {

    /**
     * 当前仅当IO操作成功完成返回true
     *
     * @return
     */
    boolean isSuccess();
}
