package java.util.concurrent;

/**
 * {@link Executors}提供一系列的工厂方法和工具类用于 {@link Executors}，{@link ExecutorService},
 * {@link ScheduledExecutorService} 和 {@link Callable}方法；
 * <ul>
 * <li> Methods that create and return an {@link ExecutorService}
 * set up with commonly useful configuration settings.
 * <li> Methods that create and return a {@link ScheduledExecutorService}
 * set up with commonly useful configuration settings.
 * <li> Methods that create and return a "wrapped" ExecutorService, that
 * disables reconfiguration by making implementation-specific methods
 * inaccessible.
 * <li> Methods that create and return a {@link ThreadFactory}
 * that sets newly created threads to a known state.
 * <li> Methods that create and return a {@link Callable}
 * out of other closure-like forms, so they can be used
 * in execution methods requiring {@code Callable}.
 * </ul>
 */
public class Executors {
    //-------------------------------------------------  Static Methods

    /**
     * @param runnable
     * @param result
     * @param <T>
     * @return
     * @throws NullPointerException 当Runnable是null时
     */
    public static <T> Callable<T> callable(Runnable runnable, T result) {
        if (runnable == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(runnable, result);
    }

    //-------------------------------------------------  Static Classes
    static class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;

        public RunnableAdapter(Runnable runnable, T value) {
            this.task = runnable;
            this.result = value;
        }

        @Override
        public T call() {
            this.task.run();
            return this.result;
        }
    }
}
