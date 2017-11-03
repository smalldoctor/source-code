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
     * 取消任务：
     * 1. 获取任务返回结果时，发生中断异常;
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
            for (int i = 0; i < futures.size(); i++) {
                Future<T> f = futures.get(i);
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
     * 对于没有完成的场景，取消能取消的任务:
     * 1. 在等待结果的过程中超时异常
     * 2. 在等待结果的过程中发生中断异常,发生中断异常
     * 3. 在任务提交过程中超时
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

            /**
             * JDK1.8修改为计算deadline，使得计算时间更加准确
             */
            final long deadline = System.currentTimeMillis() + nanos;
            final int size = futures.size();
            /**
             * 计算每个任务执行的时间，超出时间则退出
             */
            for (int i = 0; i < size; i++) {
                execute((Runnable) futures.get(i));
                nanos = deadline - System.currentTimeMillis();
                if (nanos <= 0) {
                    return futures;
                }
            }

            for (int i = 0; i < size; i++) {
                Future<T> f = futures.get(i);
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
                nanos = deadline - System.currentTimeMillis();
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

    /**
     * 因为计时和非计时都是使用同一个实现，因此在非计时场景中，对超时异常进行捕获，返回null
     *
     * @param tasks
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    /**
     * 通常情况下获取第一个正常完成的任务；
     * <p>
     * ABC三个任务，可能A任务执行完成了，但是非正常完成，则需要继续计时，获取下个任务
     * <p>
     * 如果所有任务都是异常完成（状态是Exceptional），则抛出最后一个任务的异常；
     * 如果超时，则抛出超时异常；
     * <p>
     * 不论何种情况，都会尝试取消任务尚未完成的任务；
     *
     * @param tasks
     * @param timed
     * @param nanos 指所有任务被提交之后，开始计算时间
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws IllegalArgumentException 如果任务数量为0
     */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
                              boolean timed, long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null)
            throw new NullPointerException();
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        List<Future<T>> futures = new ArrayList<Future<T>>(ntasks);
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(this);

        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            ExecutionException ee = null;
            // JDK改为计算deadline
            final long deadline = System.currentTimeMillis() + nanos;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;
            for (; ; ) {
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    } else if (active == 0) {
                        // ABC三个任务，A任务异常完成，则会获取下个任务的；
                        break;
                    } else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }
                        nanos = deadline - System.currentTimeMillis();
                    } else {
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    --active;
                    try {
                        /**
                         * 如果task非正常完成，则会抛出异常，则继续获取下个task
                         */
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }
            if (ee == null)
                ee = new ExecutionException();
            throw ee;
        } finally {
            for (Future<T> f : futures)
                f.cancel(true);
        }
    }
}
