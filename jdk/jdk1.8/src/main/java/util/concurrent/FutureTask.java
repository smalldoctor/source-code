package java.util.concurrent;

/**
 * 因为 {@code FutureTask}是实现 {@link Future}，所以是一个可以跟踪且维护的异步任务；
 * 当任务没有执行完成时，get方法会阻塞；
 * 已经完成的任务不可以取消或者重启；但是可以通过runAndReset的方法重启；
 *
 * @param <V>
 */
public class FutureTask<V> implements RunnableFuture<V> {
    //-------------------------------------------------  Instance Variables
    /**
     * task初始化状态为New，通过set，setException和cancel状态转换为
     * 结束状态；
     * During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)).
     * <p>
     * 可能的状态变化过程：
     * NEW->COMPLETING->NORMAL
     * NEW->COMPLETING->EXCEPTIONAL
     * NEW->CANCELLED
     * NEW->INTERRUPTING->INTERRUPTED
     */
    private volatile int state;

    // FutureTask封装的Callable；Runnable的实现需要转换为Callable
    private Callable<V> callable;

    //-------------------------------------------------  Static Variables
    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    private static final int CANCELLED = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED = 6;

    //-------------------------------------------------  Constructs

    /**
     * @param runnable
     * @param result   可以没有返回值，可以是null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;
    }

    //-------------------------------------------------  Instances Methods
    @Override
    public void run() {

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
