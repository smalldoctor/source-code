package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dubbo的扩展点获取的工具类；
 * 1. 自动注入关联扩展点
 * 2. 自动Wrap扩展点
 * 3. 默认获取到是Adaptive Instance,Adaptive Instance 根据Adapter注解的配置获取
 * 指定的扩展实现；
 */
public class ExtensionLoader<T> {
    //-------------------------------------------------  Static Variables
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    /**
     * 每个扩展点拥有独立的ExtensionLoader
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    //-------------------------------------------------  Instance Variables
    private final Class<?> type;

    private final ExtensionFactory objectFactory;

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        /*
        * 扩展点分为两类：
        * 1. ExtensionFactory ExtensionFactory本身也是一个扩展点，可以通过Dubbo的SPI机制进行自定义
        * 2. Extension 其他非ExtensionFactory的扩展点
        * */
        this.objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    //-------------------------------------------------  Static Methods
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        return null;
    }

    //-------------------------------------------------  Instance Variables

    /**
     * 每个扩展点都有自己的ExtensionLoader，通过ExtensionLoader获取
     * AdaptiveExtension；AdaptiveExtension自适应的扩展点适配器，从而从多个扩展点的实现中自动
     * 匹配准确的Extension;
     *
     * @return
     */
    public T getAdaptiveExtension() {
        return null;
    }
}
