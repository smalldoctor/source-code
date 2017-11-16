package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * IP and Port Help
 */
public class NetUtils {
    //-------------------------------------------------  Static Variables
    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static volatile InetAddress LOCAL_ADDRESS = null;

    //-------------------------------------------------  Static Methods

    /**
     * 检查是否是有效地址;
     * <p>
     * 1. 需要检查是否是loopback(loopback是回环地址，一种虚拟接口，用于主机自己到自己的通信，发出去的报文会回到自己)
     *
     * @param address
     * @return
     */
    private static boolean isValidAddress(InetAddress address) {
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

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress))
                return localAddress;
        } catch (Throwable e) {
            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        // 获取当前主机上所有的网络接口,遍历这些网络接口
        try {
            /**
             * 包括loopback
             */
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        // 一个网络接口可能存在多个IP地址
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    public static String getLogHost() {
        InetAddress address = LOCAL_ADDRESS;
        return address == null ? LOCALHOST : address.getHostAddress();
    }
}
