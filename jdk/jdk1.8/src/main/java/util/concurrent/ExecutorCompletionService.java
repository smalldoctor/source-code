package java.util.concurrent;

public class ExecutorCompletionService<V> implements CompletionService<V> {
    //-------------------------------------------------  Constructors


    public ExecutorCompletionService(Executor executor) {

    }

    @Override
    public Future<V> submit(Callable<V> task) {
        return null;
    }

    @Override
    public Future<V> submit(Runnable task, V result) {
        return null;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return null;
    }

    @Override
    public Future<V> poll() {
        return null;
    }

    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }
}
