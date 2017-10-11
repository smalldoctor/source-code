package io.netty.util.internal.logging;

/**
 * @Author xuecy
 * @Date: 15/11/3
 * @RealUser:Chunyang Xue
 * @Time: 10:43
 * @Package:io.netty.util.internal.logging
 * @Email:xuecy@asiainfo.com
 */

/**
 * 创建InterLogger或者变更默认logger factory。通过InterLoggerFactory指定Netty使用的logger工具。
 * 默认使用Slf4JLoggerFactory，如果Slf4JLoggerFactory不可用，使用Log4JLoggerFactory。
 * 可以修改为需要的log工具在netty的其他class加载之前，因为在其他类加载时已经初始化了，所以需要提前
 * 指定。InternalLoggerFactory.setDefaultFactory();
 * 因为各个使用logger的类都是静态初始化的，所以改变之后的logger factory只会对新加载的class产生效果。
 * 应该尽可能早的调用setDefaultFactory，而不是多次调用。
 */
public abstract class InternalLoggerFactory {
    private static volatile InternalLoggerFactory logger =
            newDefaultFactory(InternalLogger.class.getName());

    private static InternalLoggerFactory newDefaultFactory(String name) {
        InternalLoggerFactory f;
        try {
            f = new Slf4JLoggerFactory(true);
            f.newInstance(name).debug("Using slf4j as the default logging framework");
        } catch (Throwable t) {
            try {
                f = new Log4JLoggerFactory();
                f.newInstance(name).debug("Using log4J as the default logging framework");
            } catch (Throwable t1) {
                f = new JdkLoggerFactory();
                f.newInstance(name).debug("Using java.util.logging as the default logging framework");
            }
        }
        return f;
    }

    protected abstract InternalLogger newInstance(String name);

    public static InternalLoggerFactory getDefaultLoggerFactory() {
        return logger;
    }

    public static void setDefaultLoggerFactory(InternalLoggerFactory defaultLoggerFactory) {
        if (defaultLoggerFactory == null) {
            throw new NullPointerException("defaultLoggerFactory");
        }
        logger = defaultLoggerFactory;
    }

    public static InternalLogger getInstance(String name) {
        return getDefaultLoggerFactory().newInstance(name);
    }

    public static InternalLogger getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }
}
