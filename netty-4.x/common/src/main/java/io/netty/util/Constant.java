package io.netty.util;

/**
 * @Author xuecy
 * @Date: 15/11/2
 * @RealUser:Chunyang Xue
 * @Time: 13:04
 * @Package:io.netty.util
 * @Email:xuecy@asiainfo.com
 */

/**
 * 实现这个接口的实例可以用==操作符（单实例）；
 * 由ConstantPool管理
 * @param <T>
 */
public interface Constant<T extends Constant<T>> extends Comparable<T> {
    /**
     * Constant的唯一Number
     * @return
     */
    int id();

    /**
     * Constant的name
     * @return
     */
    String name();
}
