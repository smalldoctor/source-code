package org.apache.catalina;

/**
 * Server组件代表Tomcat实例，即整个Servlet容器。
 * 1. 提供监听机制，对生命周期的各个阶段进行相应的处理
 * 2. 容器级别的命名资源的实现
 * 3. 监听接受SHUTDOWN命令
 */
public interface Server extends Lifecycle {
}
