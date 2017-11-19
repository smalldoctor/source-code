package org.apache.catalina.core;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * APR是一个本地库，通过JNI方法调用本地库，提高对静态资源的处理能力;
 */
public class AprLifecycleListener implements LifecycleListener {
    //-------------------------------------------------  Static Variables
    // AprLifecycleListener是否被实例
    private static boolean instanceCreated = false;

    protected static boolean aprAvailable = false;

    protected static final Object lock = new Object();

    //-------------------------------------------------  Constructors

    public AprLifecycleListener() {
        instanceCreated = true;
    }


    //-------------------------------------------------  Instance Variables

    /**
     * BEFORE_INIT_EVENT: 尝试初始化APR库，如果初始化成功，则使用APR处理请求
     * AFTER_DESTROY_EVENT: 在Tomcat实例销毁之后，清理APR相关的工作
     *
     * @param event 事件对象 The event that has occurred
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {

    }

    //-------------------------------------------------  Static Methods
    public static boolean isAprAvailable() {
        /**
         * 因为 {@link AprLifecycleListener#isAprAvailable()}在Connector初始化时会被调用，但是在实际
         * 场景中可能不启用Apr，所以Apr不会进行初始化动作，如果不加 {@code instanceCreated}会导致
         * Apr被进行初始化，则出现不确定结果。
         * {@link AprLifecycleListener}的实例在Tomcat启动解析配置文件时进行的实例化，所以如果
         * 配置文件指定启动启用则会进行实例化，所以判断是否实例化则可以判断是否启用。
         */
        if (instanceCreated) {
            synchronized (lock) {
                init();
            }
        }
        return aprAvailable;
    }

    private static void init() {

    }
}
