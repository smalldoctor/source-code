package com.rmxue.concurrent.classloader;

import org.junit.Test;

import java.net.URL;
import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;


public class TestClassLoader {
    @Test
    public void testBootstrapClassLoader() {
        URL[] bootClasspaths = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for (int i = 0; i < bootClasspaths.length; i++) {
            URL bootClasspath = bootClasspaths[i];
            System.out.println(bootClasspath.toString());
        }
    }

    @Test
    public void testThreadContextClassLoader() {
        ServiceLoader<Driver> driverLoader = ServiceLoader.load(Driver.class);
        Iterator<Driver> drivers = driverLoader.iterator();
        while (drivers.hasNext()) {
            Driver driver = drivers.next();
            System.out.println("driver:" + driver.getClass() + ",loader:" + driver.getClass().getClassLoader());
        }
        System.out.println("current thread contextloader:" + Thread.currentThread().getContextClassLoader());
        System.out.println("ServiceLoader loader:" + ServiceLoader.class.getClassLoader());

    }
}
