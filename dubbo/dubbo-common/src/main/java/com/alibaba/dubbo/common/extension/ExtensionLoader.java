package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.Holder;
import com.alibaba.dubbo.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

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
 * <p>
 * 1. 微核心，插件式
 * 将框架本身的能力进行抽象，只保留本身的工具类，支撑类，其他都做成可扩展的存在；
 * 对与Dubbo而言：它的核心，即整个框架的支撑类是ExtensionLoader，其他的功能应用协议，传输，序列化，调用等具体功能点都是作为可扩展的存在，插件式扩展；
 * 本身也起着胶水捏合的作用；
 */
public class ExtensionLoader<T> {
    //-------------------------------------------------  Static Variables
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    /**
     * 每个扩展点拥有独立的ExtensionLoader
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * 全局的缓存扩展点实现类的对象，即每个扩展点实现是单实例的
     */
    private static final ConcurrentHashMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "/internal/";

    //-------------------------------------------------  Instance Variables
    private final Class<?> type;

    private final ConcurrentHashMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * 用于在自动注入依赖的扩展点时，获取依赖的扩展点
     */
    private final ExtensionFactory objectFactory;

    private Set<Class<?>> cachedWrapperClasses;

    /**
     * 自适器通过代理动态生成，先生成源码，再编译，代价很高，因此缓存
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    private volatile Class<?> cachedAdaptiveClass = null;

    // 自激活的扩展点;根据自激活的条件判断激活，然后根据name寻找扩展点
    /**
     * name1 -> activate1
     * name2 -> activate2
     * <p>
     * 如果在配置文件中为同一个类指定多个名字，则使用第一个名字，如name1,name3=activate1
     */
    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<>();
    /**
     * clazz1 -> name1
     * clazz2 -> name2
     * <p>
     * 如果在配置文件中为同一个类指定多个名字，则使用第一个名字，如name1,name3=clazz1
     */
    private final ConcurrentHashMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();
    /**
     * 可以同时指定多个名字:
     * 如name1,name2=clazz1
     * name1 -> clazz1
     * name2 -> clazz1   =》这种情况在{@link #cachedNames}只会存放 clazz1 -> name1
     * name3 -> clazz2
     * name4 -> clazz3
     * 但是不允许
     * name1 -> clazz1
     * name1 -> clazz2
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private String cachedDefaultName;

    // 用来存放在解析扩展点配置文件时出错的行
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

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

    /**
     * 使用ExtensionLoader的加载器
     *
     * @return
     */
    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

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
     * 动态创建AdaptiveExtension的源码；
     * 通过外层方法的线程安全，保证当前方法的线程安全;
     * <p>
     * 自适应Extension类用于通过URL，获取指定的参数，动态的匹配扩展点实现；
     *
     * @return
     */
    public String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuidler = new StringBuilder();
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
        codeBuidler.append("package " + type.getPackage().getName() + ";");
        // 导入
        // 因为动态获取真正的Extension实现需要通过ExtensionLoader获取扩展点实现
        codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
        // 类名
        codeBuidler.append("\npublic class " + type.getSimpleName() + "$Adaptive"
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

                // 因为Dubbo是URL贯穿整个调用过程，URL传递参数
                //存在URL入参
                if (urlTypeIndex != -1) {
                    // Null Point check
                    // 在创建代理类时，入参会进行重命名，格式为 arg 和 N （入参的顺序）,如arg1，arg2等
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                            urlTypeIndex);
                    code.append(s);

                    // URL对象赋值给局部变量，便于后续继续生成CODE
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

                    // 没有URL的入参，也没有可以返回URL对象的入参
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adative class for interface " + type.getName()
                                + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    // 空指针检查
                    // Null point check （包含URL对象的入参是否为空）
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                            urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    //检查获取URL是否为空（从入参获取的URL对象是否为空）
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                            urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);
                    // URL对象赋值给局部变量，便于后续继续生成CODE
                    // （）括号不能失去
                    s = String.format("%s url = arg%d.%s();", URL.class, urlTypeIndex, attribMethod);
                    code.append(s);
                }

                // Adaptive配置的匹配参数
                String[] value = adaptiveAnnotation.value();
                // 如果扩展点没有配置，则使用扩展点接口名称的单词拼接
                // 规则 : HasAdaptiveExt ->  has.adaptive.ext 作为参数名称
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                // 是否存在Invocation
                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; i++) {
                    if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
                        String s = String.format("\nif(arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\n String methodName = arg%d.getMethodName();", i);
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }

                /**
                 * 在Adaptive自适应时，依据配置SPI的默认扩展实现以及Adaptive配置的key：
                 * 1. SPI的默认值值
                 * 2. 依据Adaptive配置的key的顺序，优先级逐渐降低
                 * SPI("Impl1");Adaptive({"key1","key2"})
                 * -->url.getParameter("key1", url.getParameter("key2", "impl1"))
                 *
                 * 如果存在Invocation，则都是用getMethodParameter包含默认值的
                 */
                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; i--) {
                    if (i == value.length - 1) {
                        // 最后一个key,需要考虑默认值
                        if (defaultExtName != null) {
                            // 如果存在默认扩展点
                            if (!"protocol".equals(value[i])) {
                                if (hasInvocation) {
                                    // 如果存在Invocation，则是获取method parameter
                                    getNameCode = String.format("url.getMethodParameter(methodName,\"%s\",\"%s\")", value[i], defaultExtName);
                                } else {
                                    getNameCode = String.format("url.getParameter(\"%s\",\"%s\")", value[i], defaultExtName);
                                }
                            } else {
                                // 如果是protocol直接使用URL.getProtocol
                                getNameCode = String.format("url.getProtocol() == null ? \"%s\" : url.getProtocol()", defaultExtName);
                            }
                        } else {
                            if (!"protocol".equals(value[i])) {
                                if (hasInvocation) {
                                    // 如果存在Invocation，则是获取method parameter
                                    getNameCode = String.format("url.getMethodParameter(methodName,\"%s\",\"%s\")", value[i], defaultExtName);
                                } else {
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                                }
                            } else {
                                // 如果是protocol直接使用URL.getProtocol
                                getNameCode = "url.getProtocol()";
                            }
                        }
                    } else {
                        if (!"protocol".equals(value[i])) {
                            if (hasInvocation) {
                                // 如果存在Invocation，则是获取method parameter
                                getNameCode = String.format("url.getMethodParameter(methodName,\"%s\",\"%s\")", value[i], defaultExtName);
                            } else {
                                getNameCode = String.format("url.getParameter(\"%s\",\"%s\")", value[i], getNameCode);
                            }
                        } else {
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                        }
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);

                //获取指定的扩展点
                s = String.format("\n%s extension = (%<s)%s.getExtensionLoad(%s).getExtension();",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // 构建方法体返回值
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("\n extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    Class<?> pt = pts[i];
                    // 此处是直接调用
                    if (i != 0) {
                        code.append(",");
                    }
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (int i = 0; i < pts.length; i++) {
                Class<?> pt = pts[i];
                if (i > 0) {
                    codeBuidler.append(",");
                }
                codeBuidler.append(pt.getCanonicalName());
                codeBuidler.append(" ");
                codeBuidler.append("arg" + i);
            }
            codeBuidler.append(")");

            // 处理异常
            if (ets.length > 0) {
                codeBuidler.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    Class<?> et = ets[i];
                    if (i > 0) {
                        codeBuidler.append(", ");
                    }
                    codeBuidler.append(et.getCanonicalName());
                }
            }

            codeBuidler.append(" {");
            codeBuidler.append(code.toString());
            codeBuidler.append("\n}");
        }

        codeBuidler.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuidler.toString());
        }
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
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        // 如果name==true，则获取默认扩展点
        if (name.equals("true"))
            return getDefaultExtension();

        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }

        Object instance = holder.get();
        if (instance == null) {
            // 需要谁，就以谁为锁，避免死锁和被不同锁锁定的问题
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // 创建instance
                    holder.set(instance);
                }
            }
        }

        return (T) instance;
    }

    public T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null)
            throw findException(name);
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // 进行依赖注入
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ") could not be instantiated: " + t.getMessage(), t);
        }
        return null;
    }

    /**
     * 通过ObjectFactory进行依赖注入;
     * 要求：
     * 1. 存在set开头的共有方法
     * 2. set方法只有一个参数
     *
     * @param instance
     * @return
     */
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    public T getDefaultExtension() {
        getExtensionClasses();
        if (cachedDefaultName == null || cachedDefaultName.length() == 0
                || "true".equalsIgnoreCase(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     * 获取扩展点的实现类
     *
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        // 获取默认的extension
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            // 剔除两边的空格
            if (value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension" + type.getName()
                            + ":" + Arrays.toString(names));
                }
                if (names.length == 1)
                    cachedDefaultName = names[0];
            }
        }

        // 获取扩展点的配置信息
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        // DUBBO INTER DIRECTORY
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        // DUBBO DIRECTORY
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        // SERVICES DIRECTORY
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    /**
     * 文件中基本格式（不考虑使用过时的Extension注解的场景）：
     * 1. name1,name2...=class#...
     * <p>
     * 2. class#...
     * 3. class
     * =》如果实现类的名字以接口名称结尾，则实现类名除去接口名称的之后的名字；否则直接实现类的名字
     *
     * @param extensionClass
     * @param dir
     */
    private void loadFile(Map<String, Class<?>> extensionClass, String dir) {
        // 不论在什么目录下，都是扩展点类的全限定名作为文件名
        String fileName = dir + type.getName();
        try {
            // 因为服务的实现可能存在多个版本，即存在多个扩展点的实现
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        // 要求每个配置文件必须是UTF-8的格式
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                // 只会取 # 前面的一段
                                if (ci >= 0) line = line.substring(0, ci);
                                line = line.trim();
                                if (line.length() > 0) {
                                    try {
                                        String name = null;
                                        int i = line.indexOf("=");
                                        if (i > 0) {
                                            name = line.substring(0, i).trim();
                                            line = line.substring((i + 1)).trim();
                                        }
                                        if (line.length() > 0) {
                                            Class<?> clazz = Class.forName(line, true, classLoader);
                                            // clazz 是否是 type的子类型
                                            if (!type.isAssignableFrom(clazz)) {
                                                throw new IllegalStateException("Error when load extension class(interface: " +
                                                        type + ", class line: " + clazz.getName() + "), class "
                                                        + clazz.getName() + "is not subtype of interface.");
                                            }
                                            // 判断是自定义的自适器;
                                            // 只能存在一个自适器，不可以存在多个
                                            if (clazz.isAnnotationPresent(Adaptive.class)) {
                                                if (cachedAdaptiveClass == null) {
                                                    cachedAdaptiveClass = clazz;
                                                } else if (!cachedAdaptiveClass.equals(clazz)) {
                                                    throw new IllegalStateException("More than 1 adaptive class found: "
                                                            + cachedAdaptiveClass.getClass().getName()
                                                            + ", " + clazz.getClass().getName());
                                                }
                                            } else {
                                                // 如果是type(是接口)的实现类,且存在以接口作为参数的构造器，则是包装器;
                                                // 实现AOP
                                                try {
                                                    clazz.getConstructor(type);
                                                    Set<Class<?>> wrappers = cachedWrapperClasses;
                                                    if (wrappers == null) {
                                                        wrappers = new ConcurrentHashSet<>();
                                                        cachedWrapperClasses = wrappers;
                                                    }
                                                    wrappers.add(clazz);
                                                } catch (NoSuchMethodException e) {
                                                    // Extension的实现;必须存在无参构造器
                                                    clazz.getConstructor();
                                                    if (name == null || name.length() == 0) {
                                                        name = finaAnnotationName(clazz);
                                                        // 如果使用了老的Extension注解，但是没有配置默认值,则还有可能是null
                                                        if (name == null || name.length() == 0) {
                                                            if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                    && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                                name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                            } else {
                                                                throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                            }
                                                        }
                                                    }
                                                    String[] names = NAME_SEPARATOR.split(name);
                                                    /**
                                                     * 扩展点必须有NAME
                                                     */
                                                    if (names != null && names.length > 0) {
                                                        Activate activate = clazz.getAnnotation(Activate.class);
                                                        if (activate != null) {
                                                            cachedActivates.put(names[0], activate);
                                                        }
                                                        for (String n : names) {
                                                            if (!cachedNames.containsKey(clazz)) {
                                                                cachedNames.put(clazz, n);
                                                            }
                                                            Class<?> c = extensionClass.get(n);
                                                            if (c == null) {
                                                                extensionClass.put(n, clazz);
                                                            } else if (c != clazz) {
                                                                throw new IllegalStateException("Duplicate extension " +
                                                                        type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            }
                        } finally {
                            // 文件流必须关闭
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " +
                                type + ", class file: " + url + ") in " + url, t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    private String finaAnnotationName(Class<?> clazz) {
        Extension extension = clazz.getAnnotation(Extension.class);
        if (extension == null) {
            String name = clazz.getSimpleName();
            // 如果扩展点的实现以 xxx + type的名字
            if (name.endsWith(type.getSimpleName())) {
                name = name.substring(0, name.length() - type.getSimpleName().length());
            }
            return name.toLowerCase();
        }
        return extension.value();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }

}
