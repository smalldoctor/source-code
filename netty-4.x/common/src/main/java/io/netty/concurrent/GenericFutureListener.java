package io.netty.concurrent;

import java.util.EventListener;

/**
 * 用于监听异步操作结果；通过注册监听器然后回调的方式，接受异步操作的结果
 * The result of the asynchronous operation is notified once this listener
 * is added by calling ....;
 *
 * @param <F>
 */
public interface GenericFutureListener<F extends Future> extends EventListener {
    /**
     * 当Future关联的操作完成时被调用;
     * 通过注册监听器然后回调的方式；
     *
     * @param future
     * @throws Exception
     */
    void operationComplete(F future) throws Exception;
}
