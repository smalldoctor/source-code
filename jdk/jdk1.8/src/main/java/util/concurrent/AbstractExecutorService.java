package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
        return new FutureTask<T>(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null)
            throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    @Override
    public Future<?> submit(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    /**
     * 结束：
     * 1. 所有task都完成（异常结束的任务，正常结束的任务，被取消的任务）时才完成；
     * 2. 在get某个任务的结果时，发生异常（除执行异常和取消异常）
     * <p>
     * 如果存在没有完成的任务，则取消能取消的任务
     *
     * @param tasks
     * @param <T>
     * @return
     * @throws InterruptedException 在get时存在被中断的可能
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> ftask = newTaskFor(t);
                futures.add(ftask);
                execute(ftask);
            }
            /**
             * 需要等待其完成
             */
            for (Future<T> f : futures) {
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (ExecutionException ignore) {
                    } catch (CancellationException ignore) {
                    }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (Future<T> f : futures) {
                    f.cancel(true);
                }
            }
        }
    }

    /**
     * 计算每个任务的执行时间，当时间到达时则退出;
     * <p>
     * 对于没有完成的场景，取消能取消的任务；
     *
     * @param tasks
     * @param timeout
     * @param unit
     * @param <T>
     * @return
     * @throws InterruptedException
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (unit == null || tasks == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        List<Future<T>> futures = new ArrayList<>(tasks.size());

        boolean done = false;
        try {
            for (Callable<T> t : tasks)
                futures.add(newTaskFor(t));

            long lastTime = System.currentTimeMillis();
            /**
             * 计算每个任务执行的时间，超出时间则退出
             */
            Iterator<Future<T>> it = futures.iterator();
            while (it.hasNext()) {
                execute((Runnable) it.next());
                long now = System.currentTimeMillis();
                nanos -= now - lastTime;
                lastTime = now;
                if (nanos <= 0) {
                    return futures;
                }
            }

            for (Future<T> f : futures) {
                if (!f.isDone()) {
                    if (nanos <= 0) {
                        return futures;
                    }
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (ExecutionException ignore) {
                    } catch (CancellationException ignore) {
                    } catch (TimeoutException e) {
                        return futures;
                    }
                }
                long now = System.currentTimeMillis();
                nanos -= now - lastTime;
                lastTime = now;
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (Future<T> f : futures)
                    f.cancel(true);
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
