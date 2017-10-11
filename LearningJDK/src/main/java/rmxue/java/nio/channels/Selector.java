package rmxue.java.nio.channels;

import java.io.IOException;

/**
 * @Author: xuecy
 * @Date: 2016/10/16
 * @RealUser: Chunyang Xue
 * @Time: 18:04
 * @Package: rmxue.java.nio.channels
 * @Email: xuecy@live.com
 */
public abstract class Selector {
    /**
     * @param timeout 如果大于0,Selector阻塞timeout毫秒用于等待channel就绪;如果是0,则无限期等待;不能小于0;
     *                在等待过程中,线程阻塞;
     * @throws IOException
     */
    public abstract int select(long timeout) throws IOException;

    /**
     * 调用Selector的close方法之后,所有注册到Selector的Channel会被去注册,同时关联到Selector的资源会被释放
     * 已经被关闭的Selector再次调用close方法没有任何影响
     *
     * @throws IOException 如果发生IO错误会抛出异常
     */
    public abstract void close() throws IOException;
}
