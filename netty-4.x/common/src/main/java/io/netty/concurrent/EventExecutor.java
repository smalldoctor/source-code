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


}
