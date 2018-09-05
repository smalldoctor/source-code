package java.util.concurrent;

/**
 * 一个通过Future跟踪并且维护的Runnable;
 *
 * @param <V>
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    void run();
}
