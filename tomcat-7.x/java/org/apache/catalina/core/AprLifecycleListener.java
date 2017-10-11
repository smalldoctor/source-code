package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * APR是一个本地库，通过JNI方法调用本地库，提高对静态资源的处理能力;
 */
public class AprLifecycleListener implements LifecycleListener {
    /**
     * BEFORE_INIT_EVENT: 尝试初始化APR库，如果初始化成功，则使用APR处理请求
     * AFTER_DESTROY_EVENT: 在Tomcat实例销毁之后，清理APR相关的工作
     *
     * @param event 事件对象 The event that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }
}
