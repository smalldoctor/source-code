package org.apache.tomcat.digester;

import java.util.*;

/**
 * Rules的默认实现
 */
public class RulesBase implements Rules {

    // ----------------------------------------------------------- Instance Variables
    protected Digester digester;

    /**
     * key是pattern,value是rule列表，按照加入的顺序
     */
    protected HashMap<String, List<Rule>> caches = new HashMap<String, List<Rule>>();

    /**
     * 后续加入的rule对应的命名空间
     */
    protected String namespaceURI = null;

    /**
     * 当前Rules包含的所有的rule
     */
    protected ArrayList<Rule> rules = new ArrayList<Rule>();

    // ----------------------------------------------------------- Properties

    @Override
    public void setDigester(Digester digester) {
        this.digester = digester;
        Iterator<Rule> items = rules.iterator();
        while (items.hasNext()) {
            Rule item = items.next();
            item.setDigester(digester);
        }
    }

    // ----------------------------------------------------------- Public methods

    @Override
    public void add(String pattern, Rule rule) {
        // 自动去掉末尾的/
        int patterLength = pattern.length();
        if (patterLength > 1 && pattern.endsWith("/")) {
            pattern = pattern.substring(0, patterLength - 1);
        }

        List<Rule> list = caches.get(pattern);
        if (list == null) {
            list = new ArrayList<Rule>();
            caches.put(pattern, list);
        }
        list.add(rule);
        rules.add(rule);

        if (this.digester != null) {
            rule.setDigester(this.digester);
        }
        if (this.namespaceURI != null) {
            rule.setNamespaceURI(namespaceURI);
        }
    }

    /**
     * 返回rule需要按照add的顺序;
     * 如果没有匹配成功，返回长度为0的list
     *
     * @param namespaceURI
     * @param pattern
     * @return
     */
    public List<Rule> match(String namespaceURI, String pattern) {
        // 精确匹配
        List<Rule> ruleList = lookup(namespaceURI, pattern);
        if ((ruleList == null) ||
                (ruleList.size() == 0)) {
            //模糊匹配，但是匹配最长
            String longKey = "";
            Iterator<String> keys = this.caches.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith("*/")) {
                    if ((pattern.equals(key.substring(2)))
                            || (pattern.endsWith(key.substring(1)))) {
                        if (key.length() > longKey.length()) {
                            ruleList = lookup(namespaceURI, key);
                            longKey = key;
                        }
                    }
                }
            }
        }

        if (ruleList == null) {
            ruleList = new ArrayList<Rule>();
        }

        return ruleList;
    }

    // ----------------------------------------------------------- Protected Method
    protected List<Rule> lookup(String namespaceURI, String pattern) {
        List<Rule> list = this.caches.get(pattern);
        if (list == null) {
            return null;
        }
        if ((namespaceURI == null) || (namespaceURI.length() == 0)) {
            return list;
        }

        ArrayList<Rule> rules = new ArrayList<Rule>();
        Iterator<Rule> items = list.iterator();
        while (items.hasNext()) {
            Rule item = items.next();
            /**
             * 如果rule的namespaceURI为空，也匹配成功
             */
            if ((item.getNamespaceURI() == null)
                    || (item.getNamespaceURI().equals(namespaceURI))) {
                rules.add(item);
            }
        }

        return rules;
    }
}
