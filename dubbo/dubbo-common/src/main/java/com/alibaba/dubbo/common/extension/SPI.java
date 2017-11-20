package com.alibaba.dubbo.common.extension;

import java.lang.annotation.*;

/**
 * 标识扩展点，用于标识可扩展，会存在多个实现的的接口;
 * 通过SPI框架获取实现
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
    String value() default "";
}
