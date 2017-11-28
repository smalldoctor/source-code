package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.extension.Adaptive;

public interface NoSpiExt {
    @Adaptive
    String echo(URL url, String s);
}
