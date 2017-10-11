package org.apache.catalina.startup;

import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityConfig;
import org.apache.tomcat.res.StringManager;
import org.apache.tomcat.util.ExceptionUtils;

import java.io.File;
import java.io.IOException;

/**
 * Catalina启动程序
 */
public class Catalina {

    /**
     * 当前server的父加载器
     */
    protected ClassLoader parentClassLoader = Catalina.class.getClassLoader();

    protected static final StringManager sm = StringManager.getManager(Constants.PACKAGE);

    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        return ClassLoader.getSystemClassLoader();
    }

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Catalina() {
        setSecurityProtection();
        ExceptionUtils.preload();
    }

    /**
     * Set the security package access/protection.
     */
    protected void setSecurityProtection() {
        SecurityConfig securityConfig = SecurityConfig.newInstance();
        securityConfig.setPackageDefinition();
        securityConfig.setPackageAccess();
    }

    /**
     * start  a new server instance
     */
    public void load() {
        long t1 = System.nanoTime();

        // 初始化目录
        initDir();

        initNaming();

        /**
         * 使用Digester，使用digester解析配置文件
         */
    }

    /**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;

    protected void initNaming() {
        // Setting additional variables
        if (!useNaming) {
            log.info("Catalina naming disabled");
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                    System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            if (log.isDebugEnabled()) {
                log.debug("Setting naming prefix=" + value);
            }
            value = System.getProperty
                    (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty
                        (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                                "org.apache.naming.java.javaURLContextFactory");
            } else {
                log.debug("INITIAL_CONTEXT_FACTORY already set " + value);
            }
        }
    }

    protected void initDir() {
        String catalinaHome = System.getProperty(Globals.CATALINA_HOME_PROP);
        if (catalinaHome == null) {
            // Backwards compatibility patch for J2EE RI 1.3
            String j2eeHome = System.getProperty("com.sun.enterprise.home");
            if (j2eeHome != null) {
                catalinaHome = System.getProperty("com.sun.enterprise.home");
            } else if (System.getProperty(Globals.CATALINA_BASE_PROP) != null) {
                catalinaHome = System.getProperty(Globals.CATALINA_BASE_PROP);
            }
        }

        // last restore - for minimal/embedded case
        if (catalinaHome == null) {
            catalinaHome = System.getProperty("user.dir");
        }

        if (catalinaHome != null) {
            File home = new File(catalinaHome);
            if (!home.isAbsolute()) {
                try {
                    catalinaHome = home.getCanonicalPath();
                } catch (IOException e) {
                    catalinaHome = home.getAbsolutePath();
                }
            }
            System.setProperty(Globals.CATALINA_HOME_PROP, catalinaHome);
        }

        if (System.getProperty(Globals.CATALINA_BASE_PROP) == null) {
            System.setProperty(Globals.CATALINA_BASE_PROP,
                    catalinaHome);
        } else {
            String catalinaBase = System.getProperty(Globals.CATALINA_BASE_PROP);
            File base = new File(catalinaBase);
            if (!base.isAbsolute()) {
                try {
                    catalinaBase = base.getCanonicalPath();
                } catch (IOException e) {
                    catalinaBase = base.getAbsolutePath();
                }
            }
            System.setProperty(Globals.CATALINA_BASE_PROP, catalinaBase);
        }

        String temp = System.getProperty("java.io.tmpdir");
        if (temp == null || (!(new File(temp)).exists())
                || (!(new File(temp)).isDirectory())) {
            log.error(sm.getString("embedded.notmp", temp));
        }
    }

    private static final org.apache.juli.logging.Log log =
            org.apache.juli.logging.LogFactory.getLog(Catalina.class);

}
