package org.apache.catalina.util;

import javax.management.MBeanRegistration;

/**
 * {@link MBeanRegistration}用来行为接口，用来处理注册和去注册的事件
 */
public abstract class LifecycleMBeanBase extends LifecycleBase
        implements MBeanRegistration {
}
