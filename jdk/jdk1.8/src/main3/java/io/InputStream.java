package java.io;

import java.io.IOException;

/**
 * @Author: xuecy
 * @Date: 2016/10/15
 * @RealUser: Chunyang Xue
 * @Time: 11:07
 * @Package: rmxue.java.io
 * @Email: xuecy@live.com
 */

/**
 * 抽象类
 *
 * @since 1.0
 */
public abstract class InputStream implements Closeable {

    /***
     * read方法是一个阻塞方法,直到发生如下事件:
     * 1. 有可读数据
     * 2. 可用数据读取完毕
     * 3. 发生IO异常
     *
     * @param b
     * @return
     * @throws IOException
     */
    public int read(byte[] b) throws IOException {
        return -1;
    }

    @Override
    public void close() throws IOException {

    }
}
