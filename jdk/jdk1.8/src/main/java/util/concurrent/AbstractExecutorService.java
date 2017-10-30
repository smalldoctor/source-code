package java.util.concurrent;

/**
 * ExecutorService的基础实现；
 * <p>
 * 因为 {@link ExecutorService#submit(Runnable)}方法是返回Future的，但是 {@link Runnable}
 * 是没有返回值和不会抛出一个checked Exception；所以需要对 {@link Runnable}进行转换；
 * 通过 {@code newTaskFor}将 {@link Runnable}转换为{@link RunnableFuture}； {@link RunnableFuture}
 * 的默认实现是 {@link FutureTask}；
 * <p>
 * * <p><b>Extension example</b>. Here is a sketch of a class
 * that customizes {@link ThreadPoolExecutor} to use
 * a {@code CustomTask} class instead of the default {@code FutureTask}:
 * <pre> {@code
 * public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
 *
 *   static class CustomTask<V> implements RunnableFuture<V> {...}
 *
 *   protected <V> RunnableFuture<V> newTaskFor(Callable<V> c) {
 *       return new CustomTask<V>(c);
 *   }
 *   protected <V> RunnableFuture<V> newTaskFor(Runnable r, V v) {
 *       return new CustomTask<V>(r, v);
 *   }
 *   // ... add constructors, etc.
 * }}</pre>
 */
public abstract class AbstractExecutorService implements ExecutorService {

    /**
     * 通过使用 {@link Executors#callable(Runnable, Object)}将 {@link Runnable}转换为 {@link Callable}
     *
     * @param runnable
     * @param value
     * @param <T>
     * @return
     */
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return null;
    }
}
