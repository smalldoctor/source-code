package org.apache.catalina.connector;

import org.apache.catalina.util.LifecycleMBeanBase;

/**
 * Connector组件用于接受客户端连接，按照协议规则解析客户端请求，转换为Request和Response对象，
 * 通过路由映射将Request和Response对象传递对应host的Context的Wrapper处理；
 */
public class Connector extends LifecycleMBeanBase {
}
