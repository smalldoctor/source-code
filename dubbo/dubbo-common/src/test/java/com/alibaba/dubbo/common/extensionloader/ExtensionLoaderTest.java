package com.alibaba.dubbo.common.extensionloader;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extensionloader.activate.ActivateExt1;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

public class ExtensionLoaderTest {
    @Test
    public void test_getExtensionLoader_Null() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    containsString("Extension type == null"));
        }
    }

    @Test
    public void test_getExtensionLoader_NotInterface() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    containsString("Extension type(class com.alibaba.dubbo.common.extensionloader.ExtensionLoaderTest) is not interface"));
        }
    }

    @Test
    public void test_getExtensionLoader_NotSpiAnnotation() throws Exception {
        try {
            ExtensionLoader.getExtensionLoader(NoSpiExt.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    allOf(containsString("com.alibaba.dubbo.common.extensionloader.NoSpiExt"),
                            containsString("is not extension"),
                            containsString("WITHOUT @SPI Annotation")));
        }
    }

    @Test
    public void test_createAdaptiveExtensionClassCode() {
        try {
            ExtensionLoader<Activate> loader = ExtensionLoader.getExtensionLoader(Activate.class);
            loader.createAdaptiveExtensionClassCode();
        } catch (IllegalStateException t) {
            System.out.println(t.getMessage());
        }
    }

    @Test
    public void test_getExtension_MultiName() {
        try {
            ExtensionLoader<ActivateExt1> loader = ExtensionLoader.getExtensionLoader(ActivateExt1.class);
            loader.getDefaultExtension();
        } catch (IllegalStateException t) {
            System.out.println(t.getMessage());
        }
    }

    // 测试存在多个factory的场景
}
