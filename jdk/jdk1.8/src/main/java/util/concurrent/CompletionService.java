package java.util.concurrent;

/**
 * 用于解耦生产与消费；
 * <p>
 * 通常CompletionService使用独立的Executor执行task,维护完成任务的队列
 * <p>
 * 内存一致性：
 * submit动作 happen-before task的动作
 * task动作  happen-before take的成功返回
 *
 * @param <V>
 */
public interface CompletionService<V> {
    /**
     * @param task
     * @return
     * @throws RejectedExecutionException
     * @throws NullPointerException
     */
    Future<V> submit(Callable<V> task);

    Future<V> submit(Runnable task, V result);

    /**
     * 获取并移除next完成任务的Future；
     * 如果没有已经完成的，则阻塞并等待
     *
     * @return
     * @throws InterruptedException 等待时被中断
     */
    Future<V> take() throws InterruptedException;

    /**
     * 获取并移除next完成任务的Future；
     * 如果没有已经完成的，则返回 {@code NULL}
     *
     * @return
     */
    Future<V> poll();

    /**
     * 获取并移除next完成任务的Future；
     * 如果当前没有则等待，直到超时或者超市前出现完成的任务；如果超时，则返回null
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException 等待时被中断
     */
    Future<V> poll(long timeout, TimeUnit unit);
}
