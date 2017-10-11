package org.apache.catalina;

/**
 * 生命周期接口;组件实现这个接口，从而统一的声明周期管理
 */
public interface Lifecycle {
    // ----------------------------------------------------------- Constants

    public static final String BEFORE_INIT_EVENT = "before_init";

    public static final String AFTER_INIT_EVENT = "after_init";

    public static final String START_EVENT = "start";

    public static final String BEFORE_START_EVENT = "before_start";

    public static final String AFTER_START_EVENT = "after_start";

    public static final String STOP_EVENT = "stop";

    public static final String BEFORE_STOP_EVENT = "before_stop";

    public static final String AFTER_STOP_EVENT = "after_stop";

    public static final String BEFORE_DESTROY = "before_destroy";

    public static final String AFTER_DESTROY = "after_destroy";

    public static final String PERIODIC_DESTROY = "periodic";
}