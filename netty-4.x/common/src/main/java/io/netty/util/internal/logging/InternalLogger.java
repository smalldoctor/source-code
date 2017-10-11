package io.netty.util.internal.logging;

/**
 * @Author xuecy
 * @Date: 15/11/2
 * @RealUser:Chunyang Xue
 * @Time: 19:42
 * @Package:io.netty.util.internal.logging
 * @Email:xuecy@asiainfo.com
 */

/**
 * Netty 内部日志接口，不对外开放
 * 1. 为什么自定logger接口？
 *  因为每种日志实现对日志的实现方式不一样，如日志级别：Log4J中Trace级别是从1.2.12才有，而JDK的
 *  log实现中FINISH级别接近于Trace级别。所以屏蔽差异性需要自定义log接口及实现类，实现这些不一致。
 * 2. 封装收敛多态
 */
public interface InternalLogger {
    /**
     * logger实例的名字
     *
     * @return
     */
    String name();

    /**
     * 日志是否是 Trace Level
     *
     * @return
     */
    boolean isTraceEnabled();

    /**
     * Trace level 记录日志
     *
     * @param msg
     */
    void trace(String msg);

    /**
     * 使用指定的format和arg记录日志
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format
     * @param agr
     */
    void trace(String format, Object agr);

    void trace(String format, Object argA, Object argB);

    /**
     * 动态数组接受参数，但是有个隐藏的开销在调用此方法之前，因为jvm需要讲参数转化为数组。
     *
     * @param format
     * @param arg
     */
    void trace(String format, Object... arg);

    /**
     * 在Trace Level使用给定的消息记录一个异常
     *
     * @param msg
     * @param throwable
     */
    void trace(String msg, Throwable throwable);

    void trace(Throwable t);

    boolean isDebugEnabled();

    void debug(String msg);

    void debug(String format, Object arg);

    void debug(String format, Object argA, Object argB);

    void debug(String format, Object... arg);

    void debug(String msg, Throwable throwable);

    void debug(Throwable throwable);

    boolean isInfoEnabled();

    void info(String msg);

    void info(String format, Object arg);

    void info(String format, Object argA, Object argB);

    void info(String format, Object... arg);

    void info(String msg, Throwable throwable);

    void info(Throwable throwable);

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(String format, Object arg);

    void warn(String format, Object argA, Object argB);

    void warn(String format, Object... arg);

    void warn(String msg, Throwable throwable);

    void warn(Throwable throwable);

    boolean isErrorEnabled();

    void error(String msg);

    void error(String format, Object arg);

    void error(String format, Object argA, Object argB);

    void error(String format, Object... arg);

    void error(String msg, Throwable throwable);

    void error(Throwable throwable);

    /**
     * logger instance是否支持指定的级别
     *
     * @param level
     */
    boolean isEnable(InternalLogLevel level);

    void log(InternalLogLevel level, String msg);

    void log(InternalLogLevel level, String format, Object arg);

    void log(InternalLogLevel level, String format, Object argA, Object argB);

    void log(InternalLogLevel level, String format, Object... arg);

    void log(InternalLogLevel level, String msg, Throwable throwable);

    void log(InternalLogLevel level, Throwable throwable);
}
