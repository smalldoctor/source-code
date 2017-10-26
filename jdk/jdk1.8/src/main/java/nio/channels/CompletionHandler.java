package java.nio.channels;

/**
 * @Author: xuecy
 * @Date: 2016/10/18
 * @RealUser: Chunyang Xue
 * @Time: 23:55
 * @Package: rmxue.java.nio.channels
 * @Email: xuecy@live.com
 */

/**
 * 用于处理异步IO操作结果的handler
 *
 * @param <V> 每种异步操作的结果;如SocketChannel的读操作,连接接受等操作的结果。
 * @param <A> 每次异步操作时传入的附件
 * @since 1.7
 */
public interface CompletionHandler<V, A> {
}
