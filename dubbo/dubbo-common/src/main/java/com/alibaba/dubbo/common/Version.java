package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.security.CodeSource;

/**
 * 在异常处理时，区分异常重要性，非重要异常应该记录不应该使系统无法响应
 */
public final class Version {
    //-------------------------------------------------  Static Variables
    private static final Logger logger = LoggerFactory.getLogger(Version.class);
    // DUBBO的版本号
    private static final String VERSION = getVersion(Version.class, "2.0.0");

    //-------------------------------------------------  Static Methods
    public static String getVersion() {
        return VERSION;
    }

    /**
     * 获取版本号
     *
     * @param cls
     * @param defaultVersion
     * @return
     */
    public static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            // 可以通过JAR包的MANIFEST.MF文件中获取版本信息
            // impl版本
            String version = cls.getPackage().getImplementationVersion();
            if (version == null || version.length() == 0)
                // spec版本
                version = cls.getPackage().getSpecificationVersion();
            if (version == null || version.length() == 0) {
                // 如果JAR包中的MANIFEST中没有定义,则获取jar包名称中的的版本
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if (codeSource == null)
                    logger.info("No codeSource for class " + cls.getName() + " when getVersion, use default version " + defaultVersion);
                else {
                    String file = codeSource.getLocation().getFile();
                    // 文件名需要以jar结束
                    if (file != null && file.length() > 0 && file.endsWith("jar")) {
                        file = file.substring(0, file.length() - 4);
                        int i = file.lastIndexOf("/");
                        if (i >= 0)
                            file = file.substring(i + 1);
                        i = file.indexOf("-");
                        if (i >= 0)
                            file = file.substring(i + 1);
                        // 版本号是数字开头
                        while (file.length() > 0 && !Character.isDigit(file.charAt(0))) {
                            i = file.indexOf("-");
                            if (i >= 0)
                                file.substring(i + 1);
                            else
                                break;
                        }
                        version = file;
                    }
                }
            }
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) {
            // 忽略异常，返回缺省版本号
            logger.error("return default version, ignore exception " + e.getMessage(), e);
            return defaultVersion;
        }
    }
}
