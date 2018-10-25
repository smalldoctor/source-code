package jmx;

import org.junit.Test;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jmx.TestJmx
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-10-19 21:57
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-10-19      xuecy           v1.0.0               修改原因
 */
public class TestJmx {
    @Test
    public void testRemoteTomcat()throws Exception{
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://193.112.92.69:9004/jndi/rmi://193.112.92.69:9004/server");
        JMXConnector jmxc = JMXConnectorFactory.connect(url);
        MBeanServerConnection msc = jmxc.getMBeanServerConnection();
        System.out.println(msc.getMBeanCount());
    }
}
