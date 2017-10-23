package io.netty.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FailedFuture<V> extends CompleteFuture<V> {
    private final Throwable cause;

    /**
     * 失败的Cause不能是null
     *
     * @param executor
     * @param cause
     */
    public FailedFuture(EventExecutor executor, Throwable cause) {
        if (cause == null) {
            throw new NullPointerException();
        }
        this.cause = cause;
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
