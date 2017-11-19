package org.apache.catalina.connector;

import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.coyote.ProtocolHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.res.StringManager;

/**
 * Connector组件用于接受客户端连接，按照协议规则解析客户端请求，转换为Request和Response对象，
 * 通过路由映射将Request和Response对象传递对应host的Context的Wrapper处理；
 * <p>
 * Connector组件：
 * 1. Connector组件至少实现两个功能：协议处理和通信处理；
 */
public class Connector extends LifecycleMBeanBase {
    //-------------------------------------------------  Static Variables
    private static final Log log = LogFactory.getLog(Connector.class);

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);

    //-------------------------------------------------  Instance Variables
    /**
     * Coyote Protocol handler class name;
     * 默认是 Coyote http/1.1 protocolHandler
     */
    protected String protocolHandlerClassName =
            "org.apache.coyote.http11.Http11Protocol";

    // Coyote ProtocolHandler
    protected ProtocolHandler protocolHandler;

    //-------------------------------------------------  Constructors

    public Connector() {
        this(null);
    }

    public Connector(String protocol) {
        setProtocol(protocol);
        // 实例化ProtocolHandler
        try {
            Class<?> clazz = Class.forName(protocolHandlerClassName);
            /**
             * 如果是动态参数，则可以不传入参数
             */
            this.protocolHandler = (ProtocolHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error(sm.getString(
                    "coyoteConnector.protocolHandlerInstantiationFailed"), e);
        }
    }

    //-------------------------------------------------  Instance Variables

    /**
     * 设定协议的处理器
     * <p>
     * 1. 可以是HTTP/1.1，AJP/1.3或者具体的类名
     */
    public void setProtocol(String protocol) {
        if (AprLifecycleListener.isAprAvailable()) {
            if ("HTTP/1.1".equals(protocol)) {
                setProtocolHandlerClassName
                        ("org.apache.coyote.http11.Http11AprProtocol");
            } else if ("AJP/1.3".equals(protocol)) {
                setProtocolHandlerClassName
                        ("org.apache.coyote.ajp.AjpAprProtocol");
            } else if (protocol != null) {
                setProtocolHandlerClassName(protocol);
            } else {
                setProtocolHandlerClassName
                        ("org.apache.coyote.http11.Http11AprProtocol");
            }
        } else {
            if ("HTTP/1.1".equals(protocol)) {
                setProtocolHandlerClassName
                        ("org.apache.coyote.http11.Http11Protocol");
            } else if ("AJP/1.3".equals(protocol)) {
                setProtocolHandlerClassName
                        ("org.apache.coyote.ajp.AjpProtocol");
            } else if (protocol != null) {
                setProtocolHandlerClassName(protocol);
            }
        }
    }

    public String getProtocolHandlerClassName() {
        return protocolHandlerClassName;
    }

    public void setProtocolHandlerClassName(String protocolHandlerClassName) {
        this.protocolHandlerClassName = protocolHandlerClassName;
    }
}
