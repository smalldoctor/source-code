package com.alibaba.dubbo.common.utils;

import java.util.*;

public class CollectionUtils {
    //-------------------------------------------------  Constructors
    private CollectionUtils() {
    }

    //-------------------------------------------------  Static Methods
    private static final Comparator<String> SIMPLE_NAME_COMPARATOR = (s1, s2) -> {
        if (s1 == null && s2 == null)
            return 0;
        // s1 ， s2 有一个为null的情形
        if (s1 == null)
            return -1;
        if (s2 == null)
            return 1;
        // s1,s2 都不为null
        int i1 = s1.lastIndexOf(".");
        if (i1 >= 0)
            s1 = s1.substring(i1 + 1);
        int i2 = s2.lastIndexOf(".");
        if (i2 >= 0)
            s2 = s2.substring(i2 + 1);
        return s1.compareToIgnoreCase(s2);
    };

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> List<T> sort(List<T> list) {
        if (list != null && list.size() > 0)
            Collections.sort((List) list);
        return list;
    }

    public static List<String> sortSimpleName(List<String> list) {
        if (list != null && list.size() > 0)
            Collections.sort(list, SIMPLE_NAME_COMPARATOR);
        return list;
    }

    /**
     * key-value pair；
     * pairs必须是偶数个数；key，value，key，value。。。。
     *
     * @param pairs
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Object... pairs) {
        Map<K, V> ret = new HashMap<>();
        if (pairs == null || pairs.length == 0)
            return ret;
        if (pairs.length % 2 != 0)
            throw new IllegalArgumentException("Map pairs can not be odd number.");

        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            // key-value pair
            ret.put((K) pairs[2 * i], (V) pairs[2 * i + 1]);
        }
        return ret;
    }

    public static Map<String, String> toStringMap(String... pairs) {
        Map<String, String> result = new HashMap<>();
        if (pairs != null && pairs.length > 0) {
            if (pairs.length % 2 != 0)
                throw new IllegalArgumentException("pairs must be even.");
            for (int i = 0; i < pairs.length; i += 2) {
                result.put(pairs[i], pairs[i + 1]);
            }
        }
        return result;
    }

}
