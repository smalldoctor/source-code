package org.apache.catalina;

import java.util.EventObject;

/**
 * EventObject事件对象，根对象，衍生出具体的事件对象；
 */
public final class LifecycleEvent extends EventObject {
    /**
     * @param lifecycle 父类需要一个事件源对象；而触发事件的相关的component都已经实现Lifecycle
     * @param type
     * @param data
     */
    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {
        super(lifecycle);
    }
}
