package org.apache.coyote;

/**
 * Container是一个高层次的接口，广义代表Servlet容器，狭义上是有各种具体的容器，如Engine，Host等，这些细分的
 * Container组合成一个真正的Servlet Container；
 * <p>
 * Adapter是适配器，适配器的重要作用就是连接；
 * 将Coyote的Request和Response转换为Connector（Servlet）的Request和Response；
 */
public interface Adapter {
}
