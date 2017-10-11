package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * Jasper是Tomcat的JSP编译器核心引擎
 */
public class JasperListener implements LifecycleListener {
    /**
     * BEFOER_INIT_EVENT: 初始化Jasper组件
     *
     * @param event 事件对象
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
