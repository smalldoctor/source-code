package rmxue.java.util.concurrent.locks;

/**
 * @Author: xuecy
 * @Date: 2016/10/19
 * @RealUser: Chunyang Xue
 * @Time: 20:24
 * @Package: rmxue.java.util.concurrent.locks
 * @Email: xuecy@live.com
 */

/**
 * Synchronizer可以被用于一个线程排他拥有。它是锁实现的基础。
 * 被线程独占方式拥有的同步器。
 *
 * @since 1.6
 */
public abstract class AbstractOwnableSynchronizer implements java.io.Serializable {
    private static final long serialVersionUID = 3737899427754241961L;

    /**
     * 空的构造器,供子类调用
     */
    protected AbstractOwnableSynchronizer() {
    }

    /*排他模式Synchronizer的当前拥有者*/
    private transient Thread exclusiveOwnerThread;

    /**
     * 获取独占方式的同步器的拥有者线程;这个方法不用进行同步控制;
     *
     * @return
     */
    public final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }

    /**
     * 设置独占方式的同步器的拥有者线程;这个方法不用进行同步控制;
     *
     * @param exclusiveOwnerThread
     */
    public final void setExclusiveOwnerThread(Thread exclusiveOwnerThread) {
        this.exclusiveOwnerThread = exclusiveOwnerThread;
    }
}
