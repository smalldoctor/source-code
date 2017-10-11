package io.netty.util.internal;

/**
 * @Author xuecy
 * @Date: 15/11/2
 * @RealUser:Chunyang Xue
 * @Time: 16:23
 * @Package:io.netty.util.internal
 * @Email:xuecy@asiainfo.com
 */

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 获取和解析Java system属性的utility methods集合
 */
public final class SystemPropertyUtil {

    private static boolean initializedLogger;
    private static boolean loggedException;
    private static final InternalLogger logger;

    static {
        initializedLogger = false;
        logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);
        initializedLogger = true;
    }


    /**
     * 获取指定key的java system properties，如果没有这个属性属性
     * 或者禁止访问这个属性的时候，返回def
     */
    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        // 如果返回值为空串，也是返回true
        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return true;
        }

        if (value.equals("yes") || value.equals("1")
                || value.equals("true")) {
            return true;
        }

        if (value.equals("no") || value.equals("0")
                || value.equals("false")) {
            return false;
        }

        // log如果value不是指定的几个值打印出来，然后使用默认值返回
        return def;
    }

    /**
     * 返回指定key的java system properties，如果无法找不到这个值
     * 返回null
     *
     * @param key 系统属性key
     * @return 返回value或者null
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * 返回指定key的java system property，如果access fail则返回默认值
     *
     * @param key 指定的java system property;设置为final类型是因为获取系统属性，key就是要获取的属性
     *            所以不应在方法内部修改所需要的属性key，
     * @param def 如果access fail则返回null
     * @return
     */
    public static String get(final String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty");
        }

        String value = null;
        try {
            // 需要分析一下Security
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (Exception e) {
            // 日志记录需要好好分析一下
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * 全数字字符串的Pattern
     */
    private final static Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");

    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (INTEGER_PATTERN.matcher(value).matches()) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {

            }
        }

        log("Unable to parse the long integer system properties "
                + key + " : " + value + " - using the default value:" + def);

        return def;
    }

    private void log(String msg, Exception e) {
        if (initializedLogger) {
            logger.warn(msg, e);
        } else {
            Logger.getLogger(SystemPropertyUtil.class.getName())
                    .log(Level.WARNING, msg, e);
        }
    }

    private static void log(String msg) {
        if (initializedLogger) {
            logger.warn(msg);
        } else {
            // Use JDK logging if logger was not initialized yet.
            Logger.getLogger(SystemPropertyUtil.class.getName()).log(Level.WARNING, msg);
        }
    }

}
