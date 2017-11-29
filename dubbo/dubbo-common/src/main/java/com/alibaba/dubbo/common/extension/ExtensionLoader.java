package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.Holder;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dubbo的扩展点获取的工具类；
 * 1. 自动注入关联扩展点
 * 2. 自动Wrap扩展点
 * 3. 默认获取到是Adaptive Instance,Adaptive Instance 根据Adapter注解的配置获取
 * 指定的扩展实现；
 * <p>
 * 两种获取Extension的方式:
 * 1. 在需要自定匹配时，获取Adaptive Extension；
 * 2. 在明确获取指定的Extension时，直接通过扩展点实现的name获取；
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

    /**
     * 自适器通过代理动态生成，先生成源码，再编译，代价很高，因此缓存
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    private volatile Class<?> cachedAdaptiveClass = null;

    // 扩展点自适器创建异常时记录异常
    private volatile Throwable createAdaptiveInstanceError;

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

        // null
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");

        // 必须是接口
        if (!type.isInterface())
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");

        // 必须包含@SPI的注解,即必须是扩展点
        if (!withExtensionAnnotation(type))
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            // 直接new 一个ExtensionLoader
            EXTENSION_LOADERS.put(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    //-------------------------------------------------  Instance Variables

    /**
     * 每个扩展点都有自己的ExtensionLoader，通过ExtensionLoader获取
     * AdaptiveExtension；AdaptiveExtension自适应的扩展点适配器，从而从多个扩展点的实现中自动
     * 匹配准确的Extension;
     * 在使用SPI ExtensionFactory时默认获取自适器;
     *
     * @return
     */
    public T getAdaptiveExtension() {
        // 从缓存中获取自适器
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            // 不存在缓存的自适器时，判断是否是自适器创建错误
            if (createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            // TODO 创建 Adaptive
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }
        return null;
    }

    /**
     * 创建AdaptiveExtensionClassCode；
     * 通过外层方法的线程安全，保证当前方法的线程安全
     *
     * @return
     */
    public String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        // 检查是否存在 @Adaptive
        boolean hasAdaptiveAnnotation = false;
        Method[] methods = type.getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // 不存在需要
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        return null;
    }

    /**
     * 因为是通过先获取Extension自己的ExtensionLoader，即已经知道是获取哪个类型的扩展点实现，
     * 所以此处只需要传入name即可。
     *
     * @param name
     * @return
     */
    public T getExtension(String name) {
        return null;
    }

}
