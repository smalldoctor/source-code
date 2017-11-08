package org.apache.coyote;

import java.util.concurrent.Executor;

/**
 * 对Protocol的通用能力的抽象；
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
