package org.apache.tomcat.digester;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.security.PermissionCheck;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.SAXParser;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;

/**
 * A Digester processes an XML input stream by matching a series of
 * element nesting patterns to execute Rules that have been added prior to the start of parsing.
 */
public class Digester extends DefaultHandler2 {
    // ---------------------------------------------------------- Static Fields

    protected static IntrospectionUtils.PropertySource propertySource = null;

    static {
        String className = System.getProperty("org.apache.tomcat.util.digester.PROPERTY_SOURCE");
        if (className != null) {
            ClassLoader[] cls = new ClassLoader[]{Digester.class.getClassLoader(), Thread.currentThread().getContextClassLoader()};
            for (int i = 0; i < cls.length; i++) {
                try {
                    Class<?> clazz = Class.forName(className, true, cls[i]);
                    propertySource = (IntrospectionUtils.PropertySource) clazz.newInstance();
                    break;
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    LogFactory.getLog("org.apache.tomcat.util.digester.Digester").
                            error("Unable to load property source[" + className + "].", t);
                }
            }
        }
    }

    // --------------------------------------------------- Constructs
    public Digester() {
        super();

        if (propertySource != null) {
            source = new IntrospectionUtils.PropertySource[]{propertySource, source[0]};
        }
    }

    public Digester(SAXParser parser) {
        super();
        this.parser = parser;

        if (propertySource != null) {
            source = new IntrospectionUtils.PropertySource[]{propertySource, source[0]};
        }
    }

    public Digester(XMLReader reader) {
        super();
        this.reader = reader;

        if (propertySource != null) {
            source = new IntrospectionUtils.PropertySource[]{propertySource, source[0]};
        }
    }

    // --------------------------------------------------- Instance Variables

    protected IntrospectionUtils.PropertySource source[] = new IntrospectionUtils.PropertySource[]{
            new SystemPropertySource()};

    protected SAXParser parser = null;

    protected XMLReader reader = null;

    /**
     * Do we want to use a validating parse
     */
    protected boolean validating = false;

    /**
     * 是否进行规则校验，在丢失元素或者属性给予警告
     */
    protected boolean rulesValidation = false;

    protected Map<Class<?>, List<String>> fakeAttributes = null;

    protected boolean useContextClassLoader = false;

    protected Rules rules;

    protected ClassLoader classLoader;

    /**
     * current match pattern
     */
    protected String match = "";

    /**
     * The object stack being constructed.(正在构建的对象的栈)
     */
    protected ArrayStack<Object> stack = new ArrayStack<Object>();

    protected Object root;

    /**
     * 如果rules为空，则创建默认的rules实现
     *
     * @return
     */
    public Rules getRules() {
        if (this.rules == null) {
            this.rules = new RulesBase();
            this.rules.setDigester(this);
        }
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
        this.rules.setDigester(this);
    }

    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {

        return useContextClassLoader;

    }


    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes that are defined in various rules.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param use determines whether to use Context ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {

        useContextClassLoader = use;

    }

    public Map<Class<?>, List<String>> getFakeAttributes() {
        return fakeAttributes;
    }

    public void setFakeAttributes(Map<Class<?>, List<String>> fakeAttributes) {
        this.fakeAttributes = fakeAttributes;
    }

    /**
     * The Log to which most logging calls will be made.
     */
    protected Log log =
            LogFactory.getLog("org.apache.tomcat.util.digester.Digester");


    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    protected Log saxLog =
            LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");

    public void setRulesValidation(boolean ruleValidation) {
        this.rulesValidation = ruleValidation;
    }

    public boolean getRulesValidation() {
        return this.rulesValidation;
    }

    /**
     * Set the validating parse flag. This must be called before
     * parse() is called the first time.
     *
     * @param validating
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public boolean getValidating() {
        return this.validating;
    }

    /**
     * Add an "object create" rule.
     * 将对象的创建也作为规则之一
     *
     * @param pattern
     * @param className     默认java类名
     * @param attributeName 使用指定属性的值代替默认的类名(可选)
     */
    public void addObjectCreate(String pattern, String className, String attributeName) {
        addRule(pattern,
                new ObjectCreateRule(className, attributeName));
    }

    /**
     * 为指定的模式增加指定规则;
     * 指定rule所属的Digester对象
     *
     * @param pattern
     * @param rule
     */
    public void addRule(String pattern, Rule rule) {
        rule.setDigester(this);
        getRules().add(pattern, rule);
    }

    /**
     * 获取构建对象用的ClassLoader
     * 规则：
     * 1. setClassLoader指定的ClassLoader
     * 2. The thread context class loader, if it exists and the
     * useContextClassLoader property is set to true.
     * 3. The class loader used to load the Digester itself.
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        if (this.classLoader != null)
            return this.classLoader;

        if (this.useContextClassLoader) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }

        return this.getClass().getClassLoader();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void push(Object object) {
        if (stack.size() == 0)
            root = object;

        stack.push(object);
    }

    private class SystemPropertySource implements IntrospectionUtils.PropertySource {
        @Override
        public String getProperty(String key) {
            ClassLoader cl = getClassLoader();
            if (cl instanceof PermissionCheck) {
                Permission p = new PropertyPermission(key, "read");
                if (!((PermissionCheck) cl).check(p)) {
                    return null;
                }
            }
            return System.getProperty(key);
        }
    }
}
