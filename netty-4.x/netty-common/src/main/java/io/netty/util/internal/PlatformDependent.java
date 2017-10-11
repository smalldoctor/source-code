package io.netty.util.internal;

/**
 * @Author xuecy
 * @Date: 15/11/2
 * @RealUser:Chunyang Xue
 * @Time: 16:16
 * @Package:io.netty.util.internal
 * @Email:xuecy@asiainfo.com
 */

/**
 * 用于检查当前运行时环境各种属性的工具类，是final。
 * 如果设置系统属性io.netty.noUnsafe不可以使用sun.misc.Unsafe
 */
public final class PlatformDependent {

    private static boolean hasUnsafe0(){
        return false;
    }
}
