package rmxue.java.nio;

/**
 * @Author: xuecy
 * @Date: 2016/10/16
 * @RealUser: Chunyang Xue
 * @Time: 18:36
 * @Package: rmxue.java.nio
 * @Email: xuecy@live.com
 */

/**
 * 各类Buffer的父类
 * <p>
 * 抽象类
 */
public abstract class Buffer {
    /**
     * 翻转当前Buffer,将limit置为Position,将Position置为0
     * 在经过有序的put或者channel-read操作之后,调用此方法之后,用于有序的channel-write或者get操作。
     * @return 当前Buffer
     */
    public final Buffer flip() {
        return this;
    }
}
