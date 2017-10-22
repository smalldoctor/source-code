package io.netty.concurrent;

public interface EventExecutor extends EventExecutorGroup {
    /**
     * Override {@link EventExecutorGroup}的{@linkplain #next()};
     * 用于返回自己
     *
     * @return
     */
    @Override
    EventExecutor next();

    /**
     * 返回当前EventExecutor所属的EventExecutorGroup
     *
     * @return
     */
    EventExecutorGroup parent();

    /**
     * 调用{@link #inEventLoop(Thread)},使用{@link Thread#currentThread()}作为参数
     *
     * @return
     */
    boolean inEventLoop();

    /**
     * 如果指定的线程是就是当前EventLoop对应的Thread则返回True
     *
     * @param thread
     * @return
     */
    boolean inEventLoop(Thread thread);

    /**
     * 返回一个新的Promise
     *
     * @param <V>
     * @return
     */
    <V> Promise<V> newPromise();

    <V> ProgressivePromise<V> newProgressivePromise();

    /**
     * 1. 一个被设置为成功的Future
     * 2. {@link Future#isSuccess()}返回true
     * 3. 所有的对阻塞方法的调用都不在是阻塞的,立即返回
     * 4. 所有的{@link FutureListener}直接被notified
     *
     * @param <V>
     * @return
     */
    <V> Future<V> newSucceededFuture();

    /**
     * 1. 一个被设置为失败的Future
     * 2. {@link Future#isSuccess()}返回false
     * 3. 所有的对阻塞方法的调用都不在是阻塞的,立即返回
     * 4. 所有的{@link FutureListener}直接被notified
     *
     * @param <V>
     * @return
     */
    <V> Future<V> newFailedFuture();
}
