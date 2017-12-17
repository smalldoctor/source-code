package com.alibaba.dubbo.common.extensionloader.adaptive;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface HasAdaptiveExt {
    @Adaptive
    String echo(URL url, String s);
}

