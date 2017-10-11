package org.apache.tomcat.res;

import java.text.MessageFormat;
import java.util.*;

/**
 * 用于对国际化及字符串格式进行管理的类;
 * 以包为单位，每个包一个StringManager实例；
 * 以ResourceBundle为基础，在每个包下面以LocalString为开头的属性文件作为配置；
 */
public class StringManager {
    // 缓存
    private static int LOCAL_CACHE_SIZE = 10;

    /**
     * The ResourceBundle for this StringManager
     */
    private final ResourceBundle bundle;
    private final Locale locale;

    /**
     * 私有构造函数，通过静态方法获取实例，以保证每个包只有一个实例
     */
    private StringManager(String packageName, Locale locale) {
        String bundleName = packageName + ".LocalStrings";
        ResourceBundle bnd = null;
        try {
            bnd = ResourceBundle.getBundle(bundleName, locale);
        } catch (MissingResourceException ex) {
            // Try from the current loader (that's the case for trusted apps)
            // Should only be required if using a TC5 style classloader structure
            // where common != shared != server
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                try {
                    bnd = ResourceBundle.getBundle(bundleName, locale, cl);
                } catch (MissingResourceException ex2) {
                    // Ignore
                }
            }
        }

        bundle = bnd;
        // Get the actual locale, which may be different from the requested one
        if (bundle != null) {
            Locale bundleLocale = bundle.getLocale();
            if (bundleLocale.equals(Locale.ROOT)) {
                this.locale = Locale.ENGLISH;
            } else {
                this.locale = bundleLocale;
            }
        } else {
            this.locale = null;
        }
    }

    /**
     * Manager的缓存。key是包名，value是不同local对应的不同的StringManager的Map
     */
    private static Map<String, Map<Locale, StringManager>> managers =
            new Hashtable<String, Map<Locale, StringManager>>();

    public static StringManager getManager(String packageName) {
        return getManager(packageName, Locale.getDefault());
    }

    /**
     * Get the StringManager for a particular package and local. If a
     * manager for a package/local combination already exists, it will be reused,
     * else a new StringManager will be created and returned.
     * <p>
     * 单实例，线程安全
     *
     * @param packageName
     * @param locale
     * @return The instance associated with the given package and local
     */
    public static synchronized StringManager getManager(String packageName, Locale locale) {
        Map<Locale, StringManager> map = managers.get(packageName);

        if (map == null) {
            /**
             * 使用linkedHashMap，在map达到指定的Cache时，每次插入移除最旧的值
             */
            map = new LinkedHashMap<Locale, StringManager>(LOCAL_CACHE_SIZE, 1, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<Locale, StringManager> eldest) {
                    /**
                     * LOCAL_CACHE_SIZE - 1:
                     * 1. 如果size是10，再加一个就已经超出了。
                     * 2. 如果size是10，与其比较的是9，则不会超出
                     */
                    if (size() > (LOCAL_CACHE_SIZE - 1)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            managers.put(packageName, map);
        }

        StringManager mgr = map.get(locale);
        if (mgr == null) {
            mgr = new StringManager(packageName, locale);
            map.put(locale, mgr);
        }
        return mgr;
    }

    public static StringManager getManager(Class<?> clazz) {
        return getManager(clazz.getPackage().getName());
    }

    /**
     * Retrieve the StringManager for a list of Locales. The first StringManager
     * found will be returned.
     *
     * @param packageName      The package for which the StringManager was
     *                         requested
     * @param requestedLocales The list of Locales
     * @return the found StringManager or the default StringManager
     */
    public static StringManager getManager(String packageName,
                                           Enumeration<Locale> requestedLocales) {
        while (requestedLocales.hasMoreElements()) {
            Locale locale = requestedLocales.nextElement();
            StringManager result = getManager(packageName, locale);
            if (result.getLocale().equals(locale)) {
                return result;
            }
        }
        // Return the default
        return getManager(packageName);
    }

    /**
     * Get the Local this StringManager is associated with
     *
     * @return
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Get a String from the underlying resource bundle
     *
     * @param key
     * @return resource String matching key from underlying bundle or
     * null if not found
     */
    public String getString(String key) {
        String str = null;
        if (key == null) {
            String msg = "key may not have a null value";
            /**
             * RuntimeException
             */
            throw new IllegalArgumentException(msg);
        }

        try {
            // Avoid NPE if bundle is null and treat it like MSE
            if (bundle != null) {
                str = bundle.getString(key);
            }
        } catch (MissingResourceException mre) {
            //bad: shouldn't mask an exception the following way:
            //   str = "[cannot find message associated with key '" + key +
            //         "' due to " + mre + "]";
            //     because it hides the fact that the String was missing
            //     from the calling code.
            //good: could just throw the exception (or wrap it in another)
            //      but that would probably cause much havoc on existing
            //      code.
            //better: consistent with container pattern to
            //      simply return null.  Calling code can then do
            //      a null check.
            str = null;
        }

        return str;
    }

    /**
     * Get a String from the underlying resource bundle and format it
     * with the given set of arguments
     *
     * @param key  the key for the required String
     * @param args the values to insert into the message
     * @return the request string formatted with the provided arguments or the key
     * if the key not be found
     */
    public String getString(final String key, final Object... args) {
        String value = getString(key);
        if (value == null) {
            value = key;
        }

        MessageFormat mf = new MessageFormat(value);
        mf.setLocale(locale);
        return mf.format(args, new StringBuffer(), null).toString();
    }
}
