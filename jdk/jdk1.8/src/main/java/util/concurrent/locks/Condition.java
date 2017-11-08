package java.util.concurrent.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * wait-sets的实现包括monitor object和Synchronize；
 * Lock的是Synchronize的替代实现；Condition就是monitor object的替代实现；
 * <p>
 * 整个是wait/notify的重写；
 * <p>
 * 如生产者和消费者的中间缓存，当缓存有数据时消费数据，当缓存无数据时停止消费，等待数据生产；
 * 当缓存无数据时生产数据，当缓存数据满时停止生产等待数据消费；
 * Condition就是一个条件对象，作用类似于锁对象；在某种条件下，线程挂起在条件对象的队列中排队；
 * <pre>
 * class BoundedBuffer {
 *   <b>final Lock lock = new ReentrantLock();</b>
 *   // 通过两个monitor排队，一个用于生产者排队，一个用于消费者排队
 *   final Condition notFull  = <b>lock.newCondition(); </b>
 *   final Condition notEmpty = <b>lock.newCondition(); </b>
 *
 *   final Object[] items = new Object[100];
 *   int putptr, takeptr, count;
 *
 *   public void put(Object x) throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == items.length)
 *         <b>notFull.await();</b>
 *       items[putptr] = x;
 *       if (++putptr == items.length) putptr = 0;
 *       ++count;
 *       <b>notEmpty.signal();</b>
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 *
 *   public Object take() throws InterruptedException {
 *     <b>lock.lock();
 *     try {</b>
 *       while (count == 0)
 *         <b>notEmpty.await();</b>
 *       Object x = items[takeptr];
 *       if (++takeptr == items.length) takeptr = 0;
 *       --count;
 *       <b>notFull.signal();</b>
 *       return x;
 *     <b>} finally {
 *       lock.unlock();
 *     }</b>
 *   }
 * }
 * </pre>
 */
public interface Condition {
    /**
     * 当前线程挂起进入等待，直到被SIGNAL或者发生中断；
     * 当前线程会处于等待休眠状态(lie dormant)，发生以下场景会被唤醒：
     * 1. 其他线程调用Condition（与await相同的Condition对象） SIGNAL（随机唤醒任意一个
     * 等待的线程），同时正好唤醒是当前的线程；
     * 2. 其他线程调用Condition SignalAll
     * 3. 其他线程调用 {@link Thread#interrupt()}中断当前线程，当前线程支持中断
     * 4. 虚假唤醒
     * <p>
     * 在线程从await方法返回之前，需要先获取锁。一旦线程返回，则说明已经持有锁；
     * <p>
     * 在进入方法之前，线程中断标志已经设置或者在等待过程中发生中断，则抛出中断异常；
     * 并且中断标志被清楚；
     * <p>
     * 对中断异常的场景，需要保证signal被转发给下个等待的线程，避免无限等待；
     *
     * @throws InterruptedException
     */
    void await() throws InterruptedException;

    /**
     * 当前线程挂起进入等待，直到被SIGNAL或者发生中断；
     * 当前线程会处于等待休眠状态(lie dormant)，发生以下场景会被唤醒：
     * 1. 其他线程调用Condition（与await相同的Condition对象） SIGNAL（随机唤醒任意一个
     * 等待的线程），同时正好唤醒是当前的线程；
     * 2. 其他线程调用Condition SignalAll
     * 3. 虚假唤醒
     * <p>
     * 不考虑中断的场景，即使发生中断，也等待signal;但是会设置中断标志
     */
    void awaitUninterruptibly();

    /**
     * 当前线程挂起进入等待，直到被SIGNAL或者发生中断；
     * 当前线程会处于等待休眠状态(lie dormant)，发生以下场景会被唤醒：
     * 1. 其他线程调用Condition（与await相同的Condition对象） SIGNAL（随机唤醒任意一个
     * 等待的线程），同时正好唤醒是当前的线程；
     * 2. 其他线程调用Condition SignalAll
     * 3. 其他线程调用 {@link Thread#interrupt()}中断当前线程，当前线程支持中断
     * 4. 虚假唤醒
     * 5. 超时时间到达
     * <p>
     * *  <pre> {@code
     * boolean aMethod(long timeout, TimeUnit unit) {
     *   long nanos = unit.toNanos(timeout);
     *   lock.lock();
     *   try {
     *     while (!conditionBeingWaitedFor()) {
     *       if (nanos <= 0L)
     *         return false;
     *       nanos = theCondition.awaitNanos(nanos);
     *     }
     *     // ...
     *   } finally {
     *     lock.unlock();
     *   }
     * }}</pre>
     *
     * @param nanosTimeout
     * @return
     * @throws InterruptedException
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    boolean await(long time, TimeUnit unit) throws InterruptedException;

    boolean awaitUntil(Date deadline) throws InterruptedException;

    /**
     * 线程在从await返回之前，必须先获取锁；类似于{@link Object#wait()}；
     */
    void signal();

    void signalAll();
}
