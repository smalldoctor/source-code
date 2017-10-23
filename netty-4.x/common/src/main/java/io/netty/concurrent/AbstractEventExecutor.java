package io.netty.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Executor标识通用的执行器；
 * ExecutorService是丰富Executor，增加Executor的能力，如关闭，返回代表关闭异步操作的Future等；
 * ScheduledExecutorService是可以进行调度的Executor；
 * ThreadPoolExecutor是池实现的Executor；
 */
public abstract class AbstractEventExecutor extends AbstractExecutorService implements EventExecutor {
    //-------------------------------------------------  Static Variables
    static final long DEFAULT_SHUTDOWN_QUITE_PERIOD = 2;
    static final long DEFAULT_SHUTDOWN_TIMEOUT = 15;

    //-------------------------------------------------  Instance Variables
    /**
     * 因为是final，即EventExecutor指定归属的EventExecutorGroup之后，是不可以改变
     * 指定的EventExecutorGroup；
     */
    private final EventExecutorGroup parent;

    /**
     * Collections.singleton 返回一个包含指定对象的不可变的Set;
     * 此处用于EventExecutor的迭代器的实现
     */
    private final Collection<EventExecutor> selfCollection = Collections.<EventExecutor>singleton(this);

    //-------------------------------------------------  Constructor

    public AbstractEventExecutor() {
        this(null);
    }

    public AbstractEventExecutor(EventExecutorGroup parent) {
        this.parent = parent;
    }

    //-------------------------------------------------  Instance Method
    @Override
    public EventExecutor next() {
        return this;
    }

    @Override
    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return selfCollection.iterator();
    }

    @Override
    public Future<?> shutdownGracefully() {
        return shutdownGracefully(DEFAULT_SHUTDOWN_QUITE_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    public abstract void shutdown();

    /**
     * @deprecated {@link #shutdownGracefully(long, long, TimeUnit)} or {@link #shutdownGracefully()} instead.
     */
    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public <V> Promise<V> newPromise() {
        return new DefaultPromise<V>(this);
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return new DefaultProgressivePromise<V>(this);
    }

    @Override
    public <V> Future<V> newSucceededFuture(V result) {
        return new SucceededFuture<V>(this, result);
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable cause) {
        return new FailedFuture<V>(this, cause);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return next().submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return next().submit(task, result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return next().submit(task);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return next().schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return next().schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return next().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return next().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
