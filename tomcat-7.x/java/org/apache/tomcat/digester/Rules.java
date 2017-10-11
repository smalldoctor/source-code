package org.apache.tomcat.digester;

import java.util.List;

/**
 * 维护所有规则的映射关系，并负责在解析过程中匹配相应的规则;
 * 做成接口，便于不同项目使用的不同的匹配规则，效率等自定义
 */
public interface Rules {
    /**
     * 规则属于具体的Digester，同样Rules也是属于具体的Digester
     *
     * @param digester
     */
    public void setDigester(Digester digester);

    /**
     * 增加pattern/rule
     *
     * @param pattern
     * @param rule
     */
    void add(String pattern, Rule rule);

    List<Rule> match(String namespaceURI, String pattern);
}
