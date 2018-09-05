package java.util.concurrent;

/**
 * Executor是一个高层次的接口，用于接受并执行一个Command；
 * <p>
 * 为什么需要Executor？
 * 1. 因为Executor的实现，即Command的执行可以是多种多样的；如执行时间，可能是立即执行，调度执行，周期执行等；
 * 具体的执行者，可能是当前的调用线程，新的线程，池化的线程，或者是其他的Executor；
 * 2. 面向接口编程的体现，面向扩展的实现；通常说的线程池只是它的一种实现；
 * <p>
 * {@link ExecutorService}对{@link Executor}进行丰富之后的接口；一个更高级的Executor；
 */
public interface Executor {
    /**
     * @param command
     * @throws RejectedExecutionException
     * @throws NullPointerException
     */
    void executor(Runnable command);
}
