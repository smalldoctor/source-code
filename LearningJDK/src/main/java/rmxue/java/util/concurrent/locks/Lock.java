package rmxue.java.util.concurrent.locks;

/**
 * @Author: xuecy
 * @Date: 2016/10/17
 * @RealUser: Chunyang Xue
 * @Time: 17:51
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */

import java.util.concurrent.TimeUnit;

/**
 * Lock比使用synchronized方法和声明的代码块提供更丰富的操作;Lock支持多个Condition Object;
 * <p>
 * lock用于控制多个线程对共享资源的访问。通常情况下,lock提供对共享资源的排他访问:每次只能有一个线程获取
 * lock并且所有对共享资源的访问首先需要获取lock。但是某些lock允许对共享资源的并发访问,如ReadWriteLock的
 * read lock。
 * <p>
 * 同步方法和代码块使用的是对象关联的内部monitor lock。锁的获取和释放必须在同一个代码块,释放按照获取的
 * 相反的顺序。
 * <p>
 * 因为作用域或者代码块的方式,这样使用monitor lock进行编程变的简单,并且避免很多使用锁的相关错误,如锁的释放.
 * 但是在某些场景需要更加复杂灵活的锁解决方案。如需要获取多个锁,获取锁A,锁B,释放锁B,获取锁C,锁D等等。Lock接口
 * 的实现允许跨Scope获取和释放锁,同时按照任意顺序释放和获取锁。
 * <p>
 * 因为更加的灵活所以需要处理更过的细节。synchronized的使用自动释放锁。但是对Lock的实现,需要按照如下使用:
 * Lock l = ...;
 * l.lock();
 * try{
 * //在try-finally块中访问Lock保护的资源,防止异常的发生未能导致死锁
 * }finally{
 * // 确保最终释放锁
 * l.unlock();
 * }
 * <p>
 * lock和unlock发生在不同的scope时,所有需要被锁保护的代码在try-finally和try-catch中,确保锁能够被正常释放
 * <p>
 * Lock接口的实现提供更加丰富的锁操作,如非阻塞的方式获取锁(tryLock()),可以中断式获取锁(lockInterruptibly()),
 * 超时获取锁(tryLock(long,TimeUtil))
 * <p>
 * lock对象本身也是一个普通的对象,可以用于synchronized的代码块。除了在lock内部实现的需要,否则不建议将lock对象
 * 用于此种方式。获取lock实例的monitor lock与调用lock对象本身没有关系的
 * <p>
 * **********内存同步**************
 * 所有的Lock实现必须保证相同的内存同步语义与内置的monitor lock,具体的请参考
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * The Java Language Specification (17.4 Memory Model)
 * 1. 一个成功的lock操作与成功的Lock动作具有同等的内存同步效果
 * 2. 一个成功的unlock操作与成功Unlock动作具有同等的内存同步效果
 * <p>
 * 失败的lock和unlock操作以及reentrant的lock和unlock操作不需要内存同步的功能。
 *
 * @since 1.5
 */
public interface Lock {

    /**
     * 获取锁;
     * 如果锁不能用,当前线程将阻塞直到获取到锁。
     */
    void lock();

    /**
     * 如果在方法调用时锁是可以获取的则获取锁;
     * 如果锁是可以获取的则获取锁并且立即返回true;如果锁是不可以获取则立即返回false
     * <p>
     * 典型使用场景:
     * Lock l = ....;
     * if(l.tryLock()){
     * try{
     * 执行需要锁保护的代码
     * <p>
     * }finally{
     * l.unlock();
     * <p>
     * }
     * } else {
     * 执行无法获取锁的处理情况
     * }
     * <p>
     * 这种使用方法可以使如果获取到了锁,则一定释放锁;但是没有获取到则无需释放锁
     *
     * @return true代表获取到锁, 否则false, 代表未能获取锁
     */
    boolean tryLock();

    /**
     * 如果lock可用则获取锁并且立即返回;
     * 如果lock是不可用无法获取的,当前线程则无法调度且休眠(阻塞)直到如下两种场景发生:
     * 1. 锁被当前线程获取
     * 2. 当前线程被其他线程中断
     *
     * @throws InterruptedException
     */
    void lockInterruptibly() throws InterruptedException;

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁
     */
    void unlock();

    /***
     * 返回一个新的绑定到当前lock对象上的Condition对象。
     * 在Condition上等待之前必须先获取锁。调用Condition的await()方法之后会自动释放锁在等待之前
     * ,在等待返回之前会再次获取锁
     *
     * @return 当前lock对象的新的Condition对象
     * @throws UnsupportedOperationException 如果当前的锁不支持Condition
     */
    Condition newCondition();
}
