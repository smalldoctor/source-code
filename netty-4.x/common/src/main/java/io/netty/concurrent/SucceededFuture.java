package io.netty.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 一个已经成功完成的CompleteFuture;
 * 建议通过{@link EventExecutor#newSucceededFuture(Object)} )}获取，不建议调用构造函数,
 * 即执行和当前的EventExecutor绑定;
 * final类型，不允许Override
 *
 * @param <V>
 */
public final class SucceededFuture<V> extends CompleteFuture<V> {
    private final V result;

    /**
     * @param executor 当前Future关联的EventExecutor
     * @param result
     */
    public SucceededFuture(EventExecutor executor, V result) {
        this.result = result;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
