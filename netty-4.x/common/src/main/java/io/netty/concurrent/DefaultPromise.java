package io.netty.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultPromise<V> extends AbstractFuture<V> implements Promise<V> {
    //-------------------------------------------------  Constructors

    public DefaultPromise(EventExecutor executor) {
    }

    //-------------------------------------------------  Instance Methods

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
