package java.util.concurrent;

/**
 * @param <V> Future返回的结果的类型
 */
public interface ScheduledFuture<V> extends Delayed, Future<V> {
}
