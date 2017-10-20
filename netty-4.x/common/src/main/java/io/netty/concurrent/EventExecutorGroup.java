package io.netty.concurrent;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * EventExecutorGroup承担两个角色：
 * 1. 通过next方法提供EventExecutor
 * 2. 负责处理EventExecutor的生命周期
 */
public interface EventExecutorGroup extends ScheduledExecutorService, Iterable<EventExecutor> {
    /**
     * 返回true的场景：
     * 1. EventExecutorGroup中的所有EventExecutor正在被{@linkplain #shutdownGracefully}优雅的关闭
     * 2. {@linkplain #isShutdown()}(ExecutorService)方法返回true
     *
     * @return
     */
    boolean isShuttingDown();

    /**
     * Shortcut method for {@linkplain #shutdownGracefully(long, long, TimeUnit)};
     * 使用默认参数的实现;
     *
     * @return the {@linkplain #terminationFuture()}
     */
    Future<?> shutdownGracefully();

    /**
     * 这个方法被调用时，{@linkplain #isShuttingDown()}返回true,且executor开始关闭;
     * 与{@linkplain #shutdown()}不同，优雅关闭会在关闭自己之前存在一个quiet period：
     * 如果在quiet period没有task提交，才会关闭自己；
     * 如果在quiet period提交新的task，则接受task，并且重新计算quiet period。
     *
     * @param quietPeriod
     * @param timeout
     * @param unit
     * @return the {@linkplain #terminationFuture()}
     */
    Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit);

    /**
     * 在EventExecutorGroup中的所有EventExecutor被terminated，会notify这个Future
     *
     * @return
     */
    Future<?> terminationFuture();

    @Deprecated
    void shutdown();

    @Deprecated
    List<Runnable> shutdownNow();

    /**
     * 返回一个由{@link EventExecutorGroup}管理的EventExecutor
     *
     * @return
     */
    EventExecutor next();

    Iterator<EventExecutor> iterator();

    Future<?> submit(Runnable task);

    /**
     * 在任务成功完成时，Future的get方法会返回指定的result
     *
     * @param task
     * @param result
     * @param <T>
     * @return
     */
    <T> Future<T> submit(Runnable task, T result);

    <T> Future<T> submit(Callable<T> task);

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * command每个固定的delay之后执行
     *
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     * @return
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    /**
     * command每次执行结束之后，等待delay时间之后再次执行。
     *
     * @param command
     * @param initialDelay
     * @param delay
     * @param unit
     * @return
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
