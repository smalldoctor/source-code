package io.netty.util;

/**
 * @Author xuecy
 * @Date: 15/11/2
 * @RealUser:Chunyang Xue
 * @Time: 13:11
 * @Package:io.netty.util
 * @Email:xuecy@asiainfo.com
 */

import java.nio.ByteBuffer;

/**
 * Constant 的base implement
 *
 * @param <T>
 */
public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant {
    // 因为Constant所以用final
    private final int id;
    private final String name;
    // 唯一表示,volatile型变量，轻量级同步，保证可见行，防止double lock check失败
    // 用来表示这个常量对应的编码
    private volatile long uniquifier;
    // 待深入学习
    private ByteBuffer directBuffer;


    protected AbstractConstant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public final int id() {
        return id;
    }

    public final String name() {
        return name;
    }

    public final String toString() {
        // toString方法返回name()
        return name();
    }

    public final int hashCode() {
        return super.hashCode();
    }

    // 因为这个是个常量是单实例的，通过==进行比较的，所以实现这个接口的类
    // 的实例都是单独实例存在，所以禁止重写此equal方法。
    public final boolean equals(Object object) {
        return super.equals(object);
    }

    // 获取这个常量的对象索引
    private long uniquifier() {
        //判断当前的uniquifier是否是0
        // 如果是0则通过循环不断获取，直到获取出来的不为0
        //

        return 0;
    }

    public int compareTo(Object o) {

        return 0;
    }
}
