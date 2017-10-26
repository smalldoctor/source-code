package java.util.concurrent;

/**
 * {@link ScheduledExecutorService}是一个可以按计划执行Command的ExecutorService;
 * <p>
 * {@link Executor#execute(Runnable)}和 {@link ExecutorService}的 {@code submit}接受的
 * 任务立即执行。
 * <p>
 * {@code schedule}方法接受的相对时间的delay和period。
 * It is a simple matter to transfer an absolute time represented as {@link java.util.Date}
 * to a required form.（有个简单的方法将Date表示的时间转换为需要的格式）
 * <p>
 * * <h3>Usage Example</h3>
 * Here is a class with a method that sets up a ScheduledExecutorService
 * to beep every ten seconds for an hour:
 * <p>
 * <pre> {@code
 * import static java.util.concurrent.TimeUnit.*;
 * class BeeperControl {
 *   private final ScheduledExecutorService scheduler =
 *     Executors.newScheduledThreadPool(1);
 *
 *   public void beepForAnHour() {
 *     final Runnable beeper = new Runnable() {
 *       public void run() { System.out.println("beep"); }
 *     };
 *     final ScheduledFuture<?> beeperHandle =
 *       scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
 *     scheduler.schedule(new Runnable() {
 *       public void run() { beeperHandle.cancel(true); }
 *     }, 60 * 60, SECONDS);
 *   }
 * }}</pre>
 */
public interface ScheduledExecutorService extends ExecutorService {
    /**
     * 延期执行的非周期性的任务
     *
     * @param command
     * @param delay
     * @param unit
     * @return 在完成的时候，{@link ScheduledFuture#get()}返回null
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    /**
     * @param callable
     * @param delay
     * @param unit
     * @param <V>
     * @return 用于获取Schedule任务的结果和取消任务
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     * @return 当cancel时，get方法会抛出异常
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long period,
                                                     TimeUnit unit);
}
