package org.apache.coyote;

import java.util.concurrent.Executor;

/**
 * 对Protocol的通用能力的抽象；
 * 此处是本质是对协议进行处理，并不是定义协议；
 * <p>
 * 1. 实现类必须包含无参构造器，因为 {@link org.apache.catalina.connector.Connector}
 * 通过反射获取无参构造器创建实例
 */
public interface ProtocolHandler {
    public void getAdapter();

    public void setAdapter(Adapter adapter);

    /**
     * 协议使用的线程池
     *
     * @return
     */
    public Executor getExecutor();

    public void init() throws Exception;

    public void start() throws Exception;

    public void pause() throws Exception;

    public void resume() throws Exception;

    public void stop() throws Exception;

    public void destroy() throws Exception;

    /**
     * 使用本地APR库
     */
    public void isAprRequired();
}
