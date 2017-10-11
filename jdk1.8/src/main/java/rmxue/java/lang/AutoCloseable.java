package rmxue.java.lang;

/**
 * @Author: xuecy
 * @Date: 2016/10/15
 * @RealUser: Chunyang Xue
 * @Time: 11:09
 * @Package: rmxue.java.lang
 * @Email: xuecy@live.com
 */
/*
* 对于实现了AutoCloseable的实例,可以通过使用try-with-resource的结构进行自动的
* 资源释放
* since 1.7
* */
@SuppressWarnings("ALL")
public interface AutoCloseable {
    /**
     * @throws 资源无法被释放时抛出异常
     */
    void close() throws Exception;
}
