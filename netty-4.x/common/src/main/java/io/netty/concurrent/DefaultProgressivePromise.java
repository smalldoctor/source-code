package io.netty.concurrent;

public class DefaultProgressivePromise<V> extends DefaultPromise<V> implements ProgressivePromise<V> {
    public DefaultProgressivePromise(EventExecutor executor) {
        super(executor);
    }
}
