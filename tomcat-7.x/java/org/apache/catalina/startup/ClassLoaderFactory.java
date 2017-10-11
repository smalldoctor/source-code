package org.apache.catalina.startup;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ClassLoaderFactory {
    private static final Log log = LogFactory.getLog(ClassLoaderFactory.class);

    /**
     * 根据配置创建classLoader
     *
     * @param repositories
     * @param parent
     * @return
     */
    public static ClassLoader createClassLoader(List<Repository> repositories,
                                                final ClassLoader parent) throws Exception {
        if (log.isDebugEnabled())
            log.debug("Creating new class loader");

        /**
         * 构建jar的url
         */
        Set<URL> set = new LinkedHashSet<URL>();

        if (repositories != null) {
            for (Repository repository : repositories) {
                if (repository.getType() == RepositoryType.URL) {
                    URL url = buildClassLoaderUrl(repository.getLocation());
                    if (log.isDebugEnabled())
                        log.debug("  Including URL " + url);
                    set.add(url);
                } else if (repository.getType() == RepositoryType.DIR) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.DIR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(directory);
                    if (log.isDebugEnabled())
                        log.debug("  Including directory " + url);
                    set.add(url);
                } else if (repository.getType() == RepositoryType.JAR) {
                    File file = new File(repository.getLocation());
                    file = file.getCanonicalFile();
                    if (!validateFile(file, RepositoryType.JAR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(file);
                    if (log.isDebugEnabled())
                        log.debug("  Including jar file " + url);
                    set.add(url);
                } else if (repository.getType() == RepositoryType.GLOB) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.GLOB)) {
                        continue;
                    }
                    if (log.isDebugEnabled())
                        log.debug("  Including directory glob "
                                + directory.getAbsolutePath());
                    String filenames[] = directory.list();
                    if (filenames == null) {
                        continue;
                    }
                    for (int j = 0; j < filenames.length; j++) {
                        String filename = filenames[j].toLowerCase(Locale.ENGLISH);
                        if (!filename.endsWith(".jar"))
                            continue;
                        File file = new File(directory, filenames[j]);
                        file = file.getCanonicalFile();
                        if (!validateFile(file, RepositoryType.JAR)) {
                            continue;
                        }
                        if (log.isDebugEnabled())
                            log.debug("    Including glob jar file "
                                    + file.getAbsolutePath());
                        URL url = buildClassLoaderUrl(file);
                        set.add(url);
                    }
                }
            }
        }
        // Construct the class loader itself
        final URL[] array = set.toArray(new URL[set.size()]);
        if (log.isDebugEnabled())
            for (int i = 0; i < array.length; i++) {
                log.debug("  location " + i + " is " + array[i]);
            }

        return AccessController.doPrivileged(
                new PrivilegedAction<URLClassLoader>() {
                    @Override
                    public URLClassLoader run() {
                        if (parent == null)
                            return new URLClassLoader(array);
                        else
                            return new URLClassLoader(array, parent);
                    }
                });
    }

    private static boolean validateFile(File file,
                                        RepositoryType type) throws IOException {
        if (RepositoryType.DIR == type || RepositoryType.GLOB == type) {
            if (!file.exists() || !file.isDirectory() || !file.canRead()) {
                String msg = "Problem with directory [" + file +
                        "], exists: [" + file.exists() +
                        "], isDirectory: [" + file.isDirectory() +
                        "], canRead: [" + file.canRead() + "]";

                File home = new File(Bootstrap.getCatalinaHome());
                home = home.getCanonicalFile();
                File base = new File(Bootstrap.getCatalinaBase());
                base = base.getCanonicalFile();
                File defaultValue = new File(base, "lib");

                // Existence of ${catalina.base}/lib directory is optional.
                // Hide the warning if Tomcat runs with separate catalina.home
                // and catalina.base and that directory is absent.
                if (!home.getPath().equals(base.getPath())
                        && file.getPath().equals(defaultValue.getPath())
                        && !file.exists()) {
                    log.debug(msg);
                } else {
                    log.warn(msg);
                }
                return false;
            }
        } else if (RepositoryType.JAR == type) {
            if (!file.exists() || !file.canRead()) {
                log.warn("Problem with JAR file [" + file +
                        "], exists: [" + file.exists() +
                        "], canRead: [" + file.canRead() + "]");
                return false;
            }
        }
        return true;
    }

    private static URL buildClassLoaderUrl(String urlString) throws MalformedURLException {
        String result = urlString.replaceAll("!/", "%21/");
        return new URL(result);
    }


    private static URL buildClassLoaderUrl(File file) throws MalformedURLException {
        // Could be a directory or a file
        String fileUrlString = file.toURI().toString();
        fileUrlString = fileUrlString.replaceAll("!/", "%21/");
        return new URL(fileUrlString);
    }


    public static enum RepositoryType {
        DIR,
        GLOB,
        JAR,
        URL
    }

    public static class Repository {
        private String location;
        private RepositoryType type;

        public Repository(String location, RepositoryType type) {
            this.location = location;
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public RepositoryType getType() {
            return type;
        }
    }
}
