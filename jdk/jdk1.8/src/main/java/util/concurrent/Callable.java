package java.util.concurrent;

/**
 * Callable与 {@link Runnable}类似，都是用于线程执行的task；
 * 但是Callable与 {@link Runnable}不同：
 * 1. Runnable不能抛出checked exception
 * 2. Runnable没有返回值
 * 通过 {@link Executors}提供的工具类将其他形式的转换为Callable；
 *
 * @param <V>
 */
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
