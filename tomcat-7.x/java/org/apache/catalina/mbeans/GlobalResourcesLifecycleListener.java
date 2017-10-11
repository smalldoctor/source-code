package org.apache.catalina.mbeans;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * 用于创建和销毁全局JNDI资源的MBeans
 */
public class GlobalResourcesLifecycleListener implements LifecycleListener {
    /**
     * START_EVENT: 创建MBeans
     * STOP_EVENT：销毁MBeans
     *
     * @param event 事件对象
     */
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
