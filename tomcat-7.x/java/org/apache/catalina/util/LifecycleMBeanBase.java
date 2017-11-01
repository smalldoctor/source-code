package org.apache.catalina.util;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * {@link MBeanRegistration}用来行为接口，用来处理注册和去注册的事件
 */
public abstract class LifecycleMBeanBase extends LifecycleBase
        implements MBeanRegistration {
    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return null;
    }

    @Override
    public final void postRegister(Boolean registrationDone) {
        // NOOP
    }

    @Override
    public final void preDeregister() throws Exception {
        // NOOP
    }

    @Override
    public final void postDeregister() {
        // NOOP
    }
}
