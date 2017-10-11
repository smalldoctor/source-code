package rmxue.java.io;

/**
 * @Author: xuecy
 * @Date: 2016/10/15
 * @RealUser: Chunyang Xue
 * @Time: 11:20
 * @Package: rmxue.java.io
 * @Email: xuecy@live.com
 */

import java.io.IOException;

/**
 * 实现此接口代表data的source和destination是可以被关闭。
 * 对于拥有资源的对象调用close方法进行资源的释放
 * since 1.5
 */
@SuppressWarnings("ALL")
public interface Closeable extends AutoCloseable {
    /**
     * 如果某个资源已经关闭,调用此方法应该不要产生任何影响,即幂等方法
     *
     * @throws 如果发生IO错误,则抛出异常
     * */
    public void close() throws IOException;
}
