package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * 用来处理ThreadLocal变量的泄露问题；
 * 因为ThreadLocal保存的变量，是每个线程独有的，即相当于线程引用的。因此如果线程不
 * 销毁重新创建，则无法释放相关的对象。同时线程不销毁，则会造成ClassLoader的泄露.
 */
public class ThreadLocalLeakPreventionListener implements LifecycleListener {
    /**
     * AFTER_START_EVENT:
     * BEFORE_STOP_EVENT：
     *
     * @param event 事件对象
     */
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
