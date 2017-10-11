package org.apache.catalina;

/**
 * 统一个生命周期监听器接口
 */
public interface LifecycleListener {
    /**
     * 生命周期事件处理方法，在具体实现中对感兴趣的事件进行处理
     *
     * @param event 事件对象
     */
    public void lifecycleEvent(LifecycleEvent event);
}
