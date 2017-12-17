package com.alibaba.dubbo.common.extensionloader;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;

public interface NoSpiExt {
    @Adaptive
    String echo(URL url, String s);
}
