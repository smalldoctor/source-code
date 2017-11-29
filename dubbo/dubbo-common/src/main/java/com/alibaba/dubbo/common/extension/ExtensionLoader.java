package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.Holder;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
 * <p>
 * 两种创建Adaptive Extension的方式：
 * 1. 动态  通过识别方法级别@Adaptive 注解，动态生成Code，然后编译
 * 2. 静态 在解析SPI的配置文件，识别配置的类的类级别@Adaptive,并且有且只能有一个类的类级别使用@Adaptive
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
     * 动态
     * 创建AdaptiveExtensionClassCode；
     * 通过外层方法的线程安全，保证当前方法的线程安全;
     * <p>
     * 自适应Extension类用于通过URL，获取指定的参数，动态的匹配扩展点实现；
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

        //构建动态类的源码
        // 包名
        codeBuilder.append("package " + type.getPackage().getName() + ";");
        // 导入
        // 因为动态获取真正的Extension实现需要通过ExtensionLoader获取扩展点实现
        codeBuilder.append("\nimport " + ExtensionLoader.class.getName() + ";");
        // 类名
        codeBuilder.append("\npublic class " + type.getSimpleName() + "$Adaptive"
                + " implement " + type.getCanonicalName() + " {\n");

        // 构建代理类的类体,因为是实现接口的动态类，所以只要处理方法即可
        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            // 实现类方法体
            StringBuilder code = new StringBuilder(512);
            // 对于没有使用@Adaptive注解的方法，实现类 抛出UnsupportedOperationException异常
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                // 检查是否存在URL的方法入参，因为DUBBO使用URL对象定位资源和传递参数
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; i++) {
                    Class<?> pt = pts[i];
                    if (pt.equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }

                //存在URL入参
                if (urlTypeIndex != -1) {
                    // Null Point check
                    // 在创建代理类时，入参会进行重命名，格式为 arg 和 N （入参的顺序）,如arg1，arg2等
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                            urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                } else {
                    // 入参没有URL对象，则检查入参是否存在返回URL对象的非static的public的无参get方法
                    String attribMethod = null;

                    LBL_POINT:
                    for (int i = 0; i < pts.length; i++) {
                        // ms不会为null
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if (name.startsWith("get")
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && name.length() > 3
                                    && m.getReturnType() == URL.class) {
                                attribMethod = name;
                                // 用于构建对应的新方法的可以获取URL对象的入参的名字
                                urlTypeIndex = i;
                                break LBL_POINT;
                            }
                        }
                    }
                }
            }
        }

        logger.error("\n" + codeBuilder.toString() + "\n");
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
