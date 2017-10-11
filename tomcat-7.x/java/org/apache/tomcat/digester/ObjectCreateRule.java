package org.apache.tomcat.digester;

import org.xml.sax.Attributes;

public class ObjectCreateRule extends Rule {

    // ----------------------------------------------------------- Instance Variables
    private String className;

    private String attributeName;

    // ----------------------------------------------------------- Constructors

    /**
     * @param className     Java class name of the object to be created
     * @param attributeName Attribute name which , if present , contains an
     *                      override of the class name to create
     */
    public ObjectCreateRule(String className,
                            String attributeName) {
        this.className = className;
        this.attributeName = attributeName;
    }

    public ObjectCreateRule(String className) {
        this(className, (String) null);
    }

    public ObjectCreateRule(Class<?> clazz) {
        this(clazz.getName(), (String) null);
    }

    public ObjectCreateRule(String attributeName,
                            Class<?> clazz) {
        this(clazz.getName(), attributeName);
    }

    // ----------------------------------------------------------- Public Methods

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        // real class name
        String realClassName = className;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null) {
                realClassName = value;
            }
        }

        if (digester.log.isDebugEnabled()) {
            digester.log.debug("[ObjectCreateRule]{" + digester.match +
                    "}New " + realClassName);
        }

        if (realClassName == null) {
            throw new NullPointerException("No class name specified for " +
                    namespace + " " + name);
        }

        Class<?> clazz = digester.getClassLoader().loadClass(realClassName);
        Object instance = clazz.newInstance();
        digester.push(instance);

    }

    /**
     * Render a printable version of this Rule.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("ObjectCreateRule[");
        sb.append("className=");
        sb.append(className);
        sb.append(", attributeName=");
        sb.append(attributeName);
        sb.append("]");
        return (sb.toString());

    }
}
