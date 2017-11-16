package com.alibaba.dubbo.common.utils;

import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * IP and Port Help
 */
public class NetUtils {
    //-------------------------------------------------  Static Variables
    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    //-------------------------------------------------  Static Methods

    /**
     * 检查是否是有效地址;
     * <p>
     * 1. 需要检查是否是loopback(loopback是回环地址，一种虚拟接口，用于主机自己到自己的通信，发出去的报文会回到自己)
     *
     * @param address
     * @return
     */
    public static boolean isValidAddress(InetAddress address) {
        // 检查是否是loopback地址
        // IPV4 和 IPV6 格式不同
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return name != null && !ANYHOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches();
    }
}
