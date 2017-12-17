package com.alibaba.dubbo.common.extensionloader.activate.impl;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extensionloader.activate.ActivateExt1;

@Activate(group = {})
public class MultiNameActivateExtImp implements ActivateExt1 {
    @Override
    public String echo(String msg) {
        return "MultiName Test........";
    }
}
