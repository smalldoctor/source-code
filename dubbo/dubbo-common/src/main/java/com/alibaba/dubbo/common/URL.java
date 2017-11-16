package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.utils.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * URL，用于定位资源，并且传递参数;
 * 线程安全
 * <p>
 * Some strange example below:
 * <ul>
 * <li>192.168.1.3:20880<br>
 * for this case, url protocol = null, url host = 192.168.1.3, port = 20880, url path = null
 * <li>file:///home/user1/router.js?type=script<br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>file://home/user1/router.js?type=script<br>
 * for this case, url protocol = file, url host = home, url path = user1/router.js
 * <li>file:///D:/1/router.js?type=script<br>
 * for this case, url protocol = file, url host = null, url path = D:/1/router.js
 * <li>file:/D:/1/router.js?type=script<br>
 * same as above file:///D:/1/router.js?type=script
 * <li>/home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = null, url path = home/user1/router.js
 * <li>home/user1/router.js?type=script <br>
 * for this case, url protocol = null, url host = home, url path = user1/router.js
 * </ul>
 */
public final class URL implements Serializable {
    //-------------------------------------------------  Static Variables
    private static final long serialVersionUID = -1985165475234910535L;

    //-------------------------------------------------  Instance Variables
    /**
     * URL的标准组成部分;
     */
    private final String protocol;

    private final String username;

    private final String password;

    private final String host;

    private final int port;

    private final String path;

    private final Map<String, String> parameters;

    //-------------------------------------------------  Constructors


    /**
     * @param protocol
     * @param username
     * @param password
     * @param host
     * @param port
     * @param path
     * @param parameters
     * @throws IllegalArgumentException 如果存在密码，但是没有用户名
     */
    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        // 如果存在密码，但是没有用户名
        if ((username == null || username.length() <= 0)
                && password != null && password.length() > 0)
            throw new IllegalArgumentException("Invalid url, password without username!");
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        // port如果小于0，则默认为0
        this.port = port < 0 ? 0 : port;
        // path如果以 / 开头，则trim /
        if (path != null && path.startsWith("/"))
            path = path.substring(1);
        this.path = path;
        // 初始化parameters, 并且是不可变的;URL是线程安全；
        if (parameters == null)
            parameters = new HashMap<>();
        else
            parameters = new HashMap<>(parameters);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String[] pairs) { // 变长参数...与下面的path参数冲突，改为数组
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }
}
