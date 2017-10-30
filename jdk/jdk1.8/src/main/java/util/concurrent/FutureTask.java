package java.util.concurrent;

/**
 * 因为 {@code FutureTask}是实现 {@link Future}，所以是一个可以跟踪且维护的异步任务；
 * 当任务没有执行完成时，get方法会阻塞；
 * 已经完成的任务不可以取消或者重启；但是可以通过runAndReset的方法重启；
 * @param <V>
 */
public class FutureTask<V> implements RunnableFuture {
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
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
