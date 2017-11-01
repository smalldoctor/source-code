import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TestFutureTask {
    private Callable<Object> callable = () -> {
        System.out.println("test FutureTask............");
        System.out.println("test FutureTask start sleep");
        Thread.sleep(5000 * 5L);
        System.out.println("test FutureTask end sleep");
        return null;
    };

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() {

    }

    @Test
    public void testStatus() throws InterruptedException {
        FutureTask<?> futureTask = new FutureTask(callable);
        executorService.submit(futureTask);
        new Thread(() -> {
            System.out.println("cancel thread");
            futureTask.cancel(true);
        }).start();
        Thread.sleep(5000L);
        System.out.println(futureTask.isCancelled());
        Thread.sleep(5000*10L);
    }
}
