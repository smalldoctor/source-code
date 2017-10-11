package io.netty.util.internal.logging;

import io.netty.util.AbstractConstant;

import java.io.Serializable;

/**
 * @Author xuecy
 * @Date: 15/11/10
 * @RealUser:Chunyang Xue
 * @Time: 15:46
 * @Package:io.netty.util.internal.logging
 * @Email:xuecy@asiainfo.com
 */
public abstract class AbstractInternalLogger implements InternalLogger, Serializable {
    private static final long serialVersionUID = -6382972526573193470L;

    private static final String EXCEPTION_MESSAGE = "Unexpected exception:";

    private final String name;

    protected AbstractInternalLogger(String name) {
        if (name == null) {
            throw new NullPointerException(name);
        } else {
            this.name = name;
        }
    }

    public String name() {
        return name;
    }

    public boolean isEnable(InternalLogLevel level) {
        switch (level) {
            case TRACE:
                isTraceEnabled();
            case DEBUG:
                isDebugEnabled();
            case ERROR:
                isErrorEnabled();
            case INFO:
                isInfoEnabled();
            case WARN:
                isWarnEnabled();
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String msg) {
        switch (level) {
            case TRACE:
                trace(msg);
            case DEBUG:
                debug(msg);
            case ERROR:
                error(msg);
            case INFO:
                info(msg);
            case WARN:
                warn(msg);
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String format, Object arg) {
        switch (level) {
            case TRACE:
                trace(format, arg);
            case DEBUG:
                debug(format, arg);
            case ERROR:
                error(format, arg);
            case INFO:
                info(format, arg);
            case WARN:
                warn(format, arg);
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        switch (level) {
            case TRACE:
                trace(format, argA, argB);
            case DEBUG:
                debug(format, argA, argB);
            case ERROR:
                error(format, argA, argB);
            case INFO:
                info(format, argA, argB);
            case WARN:
                warn(format, argA, argB);
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String format, Object... arg) {
        switch (level) {
            case TRACE:
                trace(format, arg);
            case DEBUG:
                debug(format, arg);
            case ERROR:
                error(format, arg);
            case INFO:
                info(format, arg);
            case WARN:
                warn(format, arg);
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, String msg, Throwable throwable) {
        switch (level) {
            case TRACE:
                trace(msg, throwable);
            case DEBUG:
                debug(msg, throwable);
            case ERROR:
                error(msg, throwable);
            case INFO:
                info(msg, throwable);
            case WARN:
                warn(msg, throwable);
            default:
                throw new Error();
        }
    }

    public void log(InternalLogLevel level, Throwable throwable) {
        switch (level) {
            case TRACE:
                trace(throwable);
            case DEBUG:
                debug(throwable);
            case ERROR:
                error(throwable);
            case INFO:
                info(throwable);
            case WARN:
                warn(throwable);
            default:
                throw new Error();
        }
    }

    public void trace(Throwable t) {
        trace(EXCEPTION_MESSAGE, t);
    }

    public void debug(Throwable throwable) {
        debug(EXCEPTION_MESSAGE, throwable);
    }

    public void info(Throwable throwable) {
        info(EXCEPTION_MESSAGE, throwable);
    }

    public void warn(Throwable throwable) {
        warn(EXCEPTION_MESSAGE, throwable);
    }

    public void error(Throwable throwable) {
        error(EXCEPTION_MESSAGE, throwable);
    }
}
