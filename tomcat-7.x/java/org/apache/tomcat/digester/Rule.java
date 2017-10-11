package org.apache.tomcat.digester;

import org.xml.sax.Attributes;

/**
 * Concrete implementation of this class implement actions to be taken
 * when a corresponding nested pattern of XML Element has been matched。
 */
public abstract class Rule {

    // ----------------------------------------------------------- Constructors
    public Rule() {
    }


    // ----------------------------------------------------------- Instance Variables
    protected Digester digester;

    protected String namespaceURI = null;

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public Digester getDigester() {
        return digester;
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     * @deprecated Use the {@link #begin(String, String, Attributes) begin}
     * method with <code>namespace</code> and <code>name</code>
     * parameters instead.
     */
    @Deprecated
    public void begin(Attributes attributes) throws Exception {
        // The default implementation does nothing
    }


    /**
     * This method is called when the body of a matching XML element
     * is encountered.  If the element has no body, this method is
     * not called at all.
     *
     * @param text The text of the body of this element
     * @deprecated Use the {@link #body(String, String, String) body} method
     * with <code>namespace</code> and <code>name</code> parameters
     * instead.
     */
    @Deprecated
    public void body(String text) throws Exception {
        // The default implementation does nothing
    }

    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     *
     * @deprecated Use the {@link #end(String, String) end} method with
     * <code>namespace</code> and <code>name</code> parameters instead.
     */
    @Deprecated
    public void end() throws Exception {
        // The default implementation does nothing
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        begin(attributes);
    }

    /**
     * if the element has no body, this method is not called at all.
     *
     * @param namespace
     * @param name
     * @param text
     * @throws Exception
     */
    public void body(String namespace, String name, String text) throws Exception {
        body(text);
    }

    public void end(String namespace, String name) throws Exception {
        end();
    }

    /**
     * this method is called after all parsing methods have been called, to allow
     * Rules to remove temporary data.
     *
     * @throws Exception
     */
    public void finish() throws Exception {
        // the default implementation does nothing
    }
}
