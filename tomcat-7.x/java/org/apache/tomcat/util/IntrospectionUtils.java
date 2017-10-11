package org.apache.tomcat.util;

public final class IntrospectionUtils {
    public static interface PropertySource {
        public String getProperty(String key);
    }
}
