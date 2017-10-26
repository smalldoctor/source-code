package java.nio.channels;

/**
 * @Author: xuecy
 * @Date: 2016/10/16
 * @RealUser: Chunyang Xue
 * @Time: 18:16
 * @Package: rmxue.java.nio.channels
 * @Email: xuecy@live.com
 */

/***
 * 一个Token标示一个SelectableChannel被注册到Selector。被注册到Selector时创建SelectionKey。
 * <p>
 * 抽象类
 *
 * @since 1.4
 */
public abstract class SelectionKey {
    /**
     * 用来判断SelectionKey是否有效,SelectionKey被创建之后变为有效。
     * 只有其对应的Channel被关闭,或者selector被关闭,或者主动调用SelectionKey的cancel方法之后
     * 变成invalid
     *
     * @return
     */
    public abstract boolean isValid();
}
