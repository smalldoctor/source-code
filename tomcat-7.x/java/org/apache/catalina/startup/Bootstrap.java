package org.apache.catalina.startup;

import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityClassLoad;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Tomcat 启动类
 */
public final class Bootstrap {
    private static final Log log = LogFactory.getLog(Bootstrap.class);

    /**
     * Daemon object used by main
     */
    private static Bootstrap daemon = null;

    private static Object catalinaDaemon = null;

    //---类加载器
    ClassLoader commonLoader = null;
    // 容器类加载器
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;

    public static void main(String[] args) throws ClassNotFoundException {
        //daemon
        if (daemon == null) {
            // 先初始化Bootstrap,然后在设置daemon
            Bootstrap bootstrap = new Bootstrap();
            try {
                // 初始化
                bootstrap.init();
            } catch (Throwable t) {
                // 处理异常
                t.printStackTrace();
                return;
            }

            daemon = bootstrap;
        } else {
            /**
             * 保证类加载器正确，避免类找不到异常
             */
            Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("startd")) {
                args[args.length - 1] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[args.length - 1] = "stop";
                daemon.stop();
            } else if (command.equals("start")) {
                daemon.setAwait(true);
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            } else if (command.equals("configtest")) {
                daemon.load(args);
                // if server has exists
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
                System.exit(0);
            } else {
                log.warn("Bootstrap: command \"" + command + "\" does not exist.");
            }
        } catch (Throwable t) {
            // Unwrap the Exception for clearer error reporting
            if (t instanceof InvocationTargetException &&
                    t.getCause() != null) {
                t = t.getCause();
            }
            handleThrowable(t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    private Object getServer() throws Exception {
        Method method = catalinaDaemon.getClass().getMethod("getServer", (Class<?>[]) null);
        return method.invoke(catalinaDaemon, (Object[]) null);
    }

    /**
     * stop the standalone server
     *
     * @param args
     * @throws Exception
     */
    private void stopServer(String[] args) throws Exception {
        Class<?>[] paraTypes = null;
        Object[] paraValues = null;
        if ((args == null) || (args.length == 0)) {
            paraTypes = new Class[1];
            paraTypes[0] = args.getClass();
            paraValues = new Object[1];
            paraValues[0] = args;
        } else {
            // 无参
            paraTypes = null;
            paraValues = null;
        }
        Method method = catalinaDaemon.getClass().getMethod("stopServer", paraTypes);
        method.invoke(catalinaDaemon, paraValues);
    }

    private void setAwait(boolean await) throws Exception {
        Class<?>[] paraTypes = new Class<?>[1];
        paraTypes[0] = Boolean.TYPE;
        Object[] paraValues = new Object[1];
        paraValues[0] = Boolean.valueOf(await);
        Method method = catalinaDaemon.getClass().getMethod("setAwait", paraTypes);
        method.invoke(catalinaDaemon, paraValues);
    }

    private void stop() throws Exception {
        Method method = catalinaDaemon.getClass().getMethod("stop", (Class[]) null);
        method.invoke(catalinaDaemon, (Object[]) null);
    }

    private void start() throws Exception {
        if (catalinaDaemon == null) {
            init();
        }
        Method method = catalinaDaemon.getClass().getMethod("start", (Class[]) null);
        method.invoke(catalinaDaemon, (Object[]) null);
    }

    private void load(String[] args) throws Exception {
        String methodName = "load";
        Class<?>[] paraTypes;
        Object[] paraValues;
        if (args == null || args.length == 0) {
            paraTypes = null;
            paraValues = null;
        } else {
            paraTypes = new Class[]{args.getClass()};
            paraValues = new Object[]{args};
        }
        Method loadMethod =
                catalinaDaemon.getClass().getMethod(methodName, paraTypes);
        if (log.isDebugEnabled())
            log.debug("Calling startup class " + loadMethod);
        loadMethod.invoke(catalinaDaemon, paraValues);
    }

    /**
     * Initialize daemon
     *
     * @throws Exception
     */
    public void init() throws Exception {
        // set Catalina path
        setCatalinaHome();
        setCatalinaBase();

        // 初始化类加载器
        initClassLoaders();

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        SecurityClassLoad.securityClassLoad(catalinaLoader);

        // Load our startup class and call its process() method
        if (log.isDebugEnabled())
            log.debug("Loading startup class");
        Class startupClass = catalinaLoader.loadClass("org.apache.catalina.startup.Catalina");
        Object startupInstance = startupClass.newInstance();

        // Set the shared extensions class loader
        if (log.isDebugEnabled())
            log.debug("Setting startup class properties");
        String methodName = "setParentClassLoader";
        Class<?>[] paramType = new Class[]{ClassLoader.class};
        Object[] paramValue = new Object[]{sharedLoader};
        Method setParentClassLoaderMethod = startupClass.getMethod(methodName, paramType);
        setParentClassLoaderMethod.invoke(startupInstance, paramValue);

        catalinaDaemon = startupInstance;
    }

    /**
     * Catalina Home:
     * 1. 系统设置的环境变量
     * 2. 如果bootstrap不在user.dir下，则user.dir
     * 3. 如果bootstrap在user.dir下，则bootstrap所在目录的上级目录
     */
    private void setCatalinaHome() {
        // 存在
        if (System.getProperty(Globals.CATALINA_HOME_PROP) != null) {
            return;
        }

        // 不存在
        File bootstrapJar = new File(System.getProperty("user.dir"), "bootstrap.jar");
        if (bootstrapJar.exists()) {
            try {
                System.setProperty(Globals.CATALINA_HOME_PROP,
                        new File(System.getProperty("user.dir"), "..").getCanonicalPath());
            } catch (Exception e) {
                System.setProperty(Globals.CATALINA_HOME_PROP, System.getProperty("user.dir"));
            }
        } else {
            System.setProperty(Globals.CATALINA_HOME_PROP, System.getProperty("user.dir"));
        }
    }

    private void setCatalinaBase() {
        if (System.getProperty(Globals.CATALINA_BASE_PROP) != null)
            return;
        if (System.getProperty(Globals.CATALINA_HOME_PROP) != null) {
            System.setProperty(Globals.CATALINA_BASE_PROP, System.getProperty(Globals.CATALINA_HOME_PROP));
        } else {
            System.setProperty(Globals.CATALINA_BASE_PROP, System.getProperty("user.dir"));
        }
    }

    private void initClassLoaders() {
        try {
            commonLoader = createClassLoader("common", null);
            //如果commonLoader没有配置，则是parent，则为null
            if (commonLoader == null) {
                commonLoader = this.getClass().getClassLoader();
            }
            /**
             * 在没有配置的情况，则都是commonLoader.默认是没有配置的
             */
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("share", commonLoader);
        } catch (Throwable t) {
            // 处理异常
            handleThrowable(t);
            log.error("Class loader creation threw exception", t);
            System.exit(1);
        }
    }

    private ClassLoader createClassLoader(String name, ClassLoader parentLoader) throws Exception {
        /**
         * classLoader的配置;每个classLoader负责加载的jar,即类
         * 如果loader没有配置，则用父类加载器
         */
        String value = CatalinaProperties.getProperty(name + ".loader");
        if ((value == null) || (value.equals(""))) {
            return parentLoader;
        }

        // 将环境变量设置为具体的路径
        value = replace(value);

        List<ClassLoaderFactory.Repository> repositories = new ArrayList<ClassLoaderFactory.Repository>();

        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String repository = tokenizer.nextToken().trim();
            if (repository.length() == 0) {
                continue;
            }

            // Check for a JAR URL repository
            try {
                @SuppressWarnings("unused")
                URL url = new URL(repository);
                repositories.add(
                        new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.URL));
                continue;
            } catch (MalformedURLException e) {
                // Ignore
            }

            // Local repository
            if (repository.endsWith("*.jar")) {
                repository = repository.substring
                        (0, repository.length() - "*.jar".length());
                repositories.add(
                        new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(
                        new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.JAR));
            } else {
                repositories.add(
                        new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.DIR));
            }
        }

        return ClassLoaderFactory.createClassLoader(repositories, parentLoader);
    }

    /**
     * System property replacement in the given string.
     *
     * @param str The original string
     * @return the modified string
     */
    protected String replace(String str) {
        // Implementation is copied from ClassLoaderLogManager.replace(),
        // but added special processing for catalina.home and catalina.base.
        String result = str;
        int pos_start = str.indexOf("${");
        if (pos_start >= 0) {
            StringBuilder builder = new StringBuilder();
            int pos_end = -1;
            while (pos_start >= 0) {
                builder.append(str, pos_end + 1, pos_start);
                pos_end = str.indexOf('}', pos_start + 2);
                if (pos_end < 0) {
                    pos_end = pos_start - 1;
                    break;
                }
                String propName = str.substring(pos_start + 2, pos_end);
                String replacement;
                if (propName.length() == 0) {
                    replacement = null;
                } else if (Globals.CATALINA_HOME_PROP.equals(propName)) {
                    replacement = getCatalinaHome();
                } else if (Globals.CATALINA_BASE_PROP.equals(propName)) {
                    replacement = getCatalinaBase();
                } else {
                    replacement = System.getProperty(propName);
                }
                if (replacement != null) {
                    builder.append(replacement);
                } else {
                    builder.append(str, pos_start, pos_end + 1);
                }
                pos_start = str.indexOf("${", pos_end + 1);
            }
            builder.append(str, pos_end + 1, str.length());
            result = builder.toString();
        }
        return result;
    }

    /**
     * Get the value of the catalina.home environment variable.
     */
    public static String getCatalinaHome() {
        return System.getProperty(Globals.CATALINA_HOME_PROP,
                System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    public static String getCatalinaBase() {
        return System.getProperty(Globals.CATALINA_BASE_PROP, getCatalinaHome());
    }


    // Copied from ExceptionUtils since that class is not visible during start
    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

}
