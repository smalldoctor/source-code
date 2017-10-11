package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * 内存泄露： 在垃圾回收，应该被回收的对象没有回收;
 * Tomcat通过重新生成类加载器的方式重新加载Web应用。
 * 1. 某些JRE 类库的类是单实例，拥有ClassLoader，这些单实例是长期存在，则会导致ClassLoader没法释放
 * 2. 线程启动新线程时，新线程的Context Class Loader则被持有。新线程一直循环，
 * <p>
 * 锁文件：
 * URLConnection读取jar文件，因为默认使用缓存，则无法在重载WEB应用时，删除这个jar文件。
 */
public class JreMemoryLeakPreventionListener implements LifecycleListener {
    /**
     * BEFORE_INIT_EVENT: 预加载并初始化相关实例
     *
     * @param event 事件对象
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {


    }
}
