package org.apache.tomcat.digester;

import org.apache.catalina.Globals;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Digest {

    private static final org.apache.juli.logging.Log log =
            org.apache.juli.logging.LogFactory.getLog(Digest.class);

    /**
     * Pathname to the server configuration file.
     */
    protected static String configFile = "conf/server.xml";

    /**
     * Return a File object representing our configuration file.
     */
    protected static File configFile() {

        File file = new File(configFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty(Globals.CATALINA_BASE_PROP), configFile);
        }
        return (file);

    }

    public static String getConfigFile() {
        return configFile;
    }

    /**
     * Create and configure the Digester we will be using for startup.
     */
    protected Digester createStartDigester() {
        long t1 = System.currentTimeMillis();
        // Initialize the digester
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRulesValidation(true);
        HashMap<Class<?>, List<String>> fakeAttributes =
                new HashMap<Class<?>, List<String>>();
        ArrayList<String> attrs = new ArrayList<String>();
        attrs.add("className");
        fakeAttributes.put(Object.class, attrs);
        digester.setFakeAttributes(fakeAttributes);
        digester.setUseContextClassLoader(true);

        // Configure the actions we will be using
        digester.addObjectCreate("Server",
                "org.apache.catalina.core.StandardServer",
                "className");
//        digester.addSetProperties("Server");
//        digester.addSetNext("Server",
//                "setServer",
//                "org.apache.catalina.Server");
//
//        digester.addObjectCreate("Server/GlobalNamingResources",
//                "org.apache.catalina.deploy.NamingResources");
//        digester.addSetProperties("Server/GlobalNamingResources");
//        digester.addSetNext("Server/GlobalNamingResources",
//                "setGlobalNamingResources",
//                "org.apache.catalina.deploy.NamingResources");
//
//        digester.addObjectCreate("Server/Listener",
//                null, // MUST be specified in the element
//                "className");
//        digester.addSetProperties("Server/Listener");
//        digester.addSetNext("Server/Listener",
//                "addLifecycleListener",
//                "org.apache.catalina.LifecycleListener");
//
//        digester.addObjectCreate("Server/Service",
//                "org.apache.catalina.core.StandardService",
//                "className");
//        digester.addSetProperties("Server/Service");
//        digester.addSetNext("Server/Service",
//                "addService",
//                "org.apache.catalina.Service");
//
//        digester.addObjectCreate("Server/Service/Listener",
//                null, // MUST be specified in the element
//                "className");
//        digester.addSetProperties("Server/Service/Listener");
//        digester.addSetNext("Server/Service/Listener",
//                "addLifecycleListener",
//                "org.apache.catalina.LifecycleListener");
//
//        //Executor
//        digester.addObjectCreate("Server/Service/Executor",
//                "org.apache.catalina.core.StandardThreadExecutor",
//                "className");
//        digester.addSetProperties("Server/Service/Executor");
//
//        digester.addSetNext("Server/Service/Executor",
//                "addExecutor",
//                "org.apache.catalina.Executor");
//
//
//        digester.addRule("Server/Service/Connector",
//                new ConnectorCreateRule());
//        digester.addRule("Server/Service/Connector",
//                new SetAllPropertiesRule(new String[]{"executor"}));
//        digester.addSetNext("Server/Service/Connector",
//                "addConnector",
//                "org.apache.catalina.connector.Connector");
//
//
//        digester.addObjectCreate("Server/Service/Connector/Listener",
//                null, // MUST be specified in the element
//                "className");
//        digester.addSetProperties("Server/Service/Connector/Listener");
//        digester.addSetNext("Server/Service/Connector/Listener",
//                "addLifecycleListener",
//                "org.apache.catalina.LifecycleListener");
//
//        // Add RuleSets for nested elements
//        digester.addRuleSet(new NamingRuleSet("Server/GlobalNamingResources/"));
//        digester.addRuleSet(new EngineRuleSet("Server/Service/"));
//        digester.addRuleSet(new HostRuleSet("Server/Service/Engine/"));
//        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/"));
//        addClusterRuleSet(digester, "Server/Service/Engine/Host/Cluster/");
//        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/Context/"));
//
//        // When the 'engine' is found, set the parentClassLoader.
//        digester.addRule("Server/Service/Engine",
//                new SetParentClassLoaderRule(parentClassLoader));
//        addClusterRuleSet(digester, "Server/Service/Engine/Cluster/");

        long t2 = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Digester for server.xml created " + (t2 - t1));
        }
        return (digester);

    }

    @Test
    public void testDigester() {
// Create and execute our Digester
        Digester digester = createStartDigester();

        InputSource inputSource = null;
        InputStream inputStream = null;
        File file = null;
        try {
            try {
                file = configFile();
                inputStream = new FileInputStream(file);
                inputSource = new InputSource(file.toURI().toURL().toString());
            } catch (Exception e) {
            }
            if (inputStream == null) {
                try {
                    inputStream = getClass().getClassLoader()
                            .getResourceAsStream(getConfigFile());
                    inputSource = new InputSource
                            (getClass().getClassLoader()
                                    .getResource(getConfigFile()).toString());
                } catch (Exception e) {
                }
            }

            // This should be included in catalina.jar
            // Alternative: don't bother with xml, just create it manually.
            if (inputStream == null) {
                try {
                    inputStream = getClass().getClassLoader()
                            .getResourceAsStream("server-embed.xml");
                    inputSource = new InputSource
                            (getClass().getClassLoader()
                                    .getResource("server-embed.xml").toString());
                } catch (Exception e) {
                }
            }


            if (inputStream == null || inputSource == null) {
                if (file == null) {
                } else {
                }
                return;
            }

//            try {
//                inputSource.setByteStream(inputStream);
//                digester.push(this);
//                digester.parse(inputSource);
//            } catch (SAXParseException spe) {
//                return;
//            } catch (Exception e) {
//                return;
//            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
