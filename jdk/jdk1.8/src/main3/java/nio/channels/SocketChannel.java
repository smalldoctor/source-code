package java.nio.channels;

/**
 * @Author: xuecy
 * @Date: 2016/10/16
 * @RealUser: Chunyang Xue
 * @Time: 18:52
 * @Package: rmxue.java.nio.channels
 * @Email: xuecy@live.com
 */

import java.nio.ByteBuffer;

import java.io.IOException;

/**
 * 套接字Channel
 * <p>
 * 抽象类
 *
 * @since 1.4
 */
public abstract class SocketChannel {

    /***
     * ByteChannel操作;
     * 在设置为Channel设置为异步非阻塞模式时,read方法是立即返回;
     * 可以通过read方法返回的字节数进行判读:
     * 1. 大于0,读取到字节
     * 2. 等于0,没有读取到字节,对端未输出信息
     * 3. 等于-1,说明链路关闭,关闭Channel,释放资源
     *
     * @param dst
     * @return
     * @throws IOException
     */
    public abstract int read(ByteBuffer dst) throws IOException;

    /**
     * ByteChannel操作;
     * TCP协议的实现是存在缓冲区的,TCP通过TCP缓冲区进行读写,在TCP window size变为0之后是无法继续写数据的。
     * 通常使用阻塞IO,write操作则会一直阻塞。
     * 在Channel是非阻塞模式时,因此无法保证一次将Buffer中所有的数据都写出,因此会出现半包的情况;
     * 解决方法:
     * 1. 通过Selector注册Channel的写操作,不断轮询
     * 2. 在可写入时继续写入剩余的数据
     * 3. 通过ByteBuffer的hasRemain()或者其他方式判断消息是否已经发送完成
     *
     * @param src
     * @return
     * @throws IOException
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /***
     * 完成Channel的连接操作
     * <p>
     * 在异步非阻塞模式下提交一个SocketChannel的非阻塞connect操作之后,因为某些原因可能未能立即成功建立连接,
     * 如网络延迟等,调用finishConnect方法完成连接建立。
     *
     * @return
     * @throws IOException
     */
    public abstract boolean finishConnect() throws IOException;
}
