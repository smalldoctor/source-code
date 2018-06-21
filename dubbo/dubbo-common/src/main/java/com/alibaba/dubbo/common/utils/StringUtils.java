package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.io.UnsafeStringWriter;

import java.io.PrintWriter;

/**
 * 字符串工具类
 */
public final class StringUtils {
    //-------------------------------------------------  Static Methods

    /**
     * 类名：信息
     * 堆栈
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.write(e.getClass().getName());
        if (e.getMessage() != null)
            p.write(":" + e.getMessage());
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }
}
