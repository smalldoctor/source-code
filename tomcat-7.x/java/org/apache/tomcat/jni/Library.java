package org.apache.tomcat.jni;

import java.io.File;

/**
 * JNI 本地库工具类
 */
public final class Library {
    //-------------------------------------------------  Static Variables
    // 本地动态链接库的名字
    private static final String[] NAMES = {"tcnative-1", "libtcnative-1"};

    /**
     * 单实例
     */
    private static Library _instance = null;

    //-------------------------------------------------  Constructors
    // 指定libraryName
    private Library(String libraryName) {
        System.loadLibrary(libraryName);
    }

    // 通过系统参数获取libraryName
    private Library() {
        // 获取library路径
        String path = System.getProperty("java.library.path");
        String[] paths = path.split(File.pathSeparator);
        boolean loaded = false;
        StringBuilder err = new StringBuilder();
        for (int i = 0; i < NAMES.length; i++) {
            try {
                System.loadLibrary(NAMES[i]);
                loaded = true;
            } catch (ThreadDeath t) {
                throw t;
            } catch (VirtualMachineError t) {
                // Don't use a Java 7 multiple exception catch so we can keep
                // the JNI code identical between Tomcat 6/7/8/9
                throw t;
            } catch (Throwable t) {
                String name = System.mapLibraryName(NAMES[i]);
                for (int j = 0; j < paths.length; j++) {
                    java.io.File fd = new java.io.File(paths[j], name);
                    if (fd.exists()) {
                        // File exists but failed to load
                        throw t;
                    }
                }
                if (i > 0) {
                    err.append(", ");
                }
                err.append(t.getMessage());
            }
            if (loaded) {
                break;
            }
        }
        if (!loaded) {
            StringBuilder names = new StringBuilder();
            for (String name : NAMES) {
                names.append(name);
                names.append(", ");
            }
            throw new LibraryNotFoundError(names.substring(0, names.length() - 2), err.toString());
        }
    }
}
