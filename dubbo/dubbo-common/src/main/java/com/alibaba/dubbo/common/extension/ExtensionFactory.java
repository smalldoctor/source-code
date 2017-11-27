package com.alibaba.dubbo.common.extension;

public interface ExtensionFactory {
    <T> T getExtension(Class<T> type, String name);
}
