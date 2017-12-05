package com.alibaba.dubbo.common.extension;

import java.lang.annotation.*;

/**
 * 用于标识适配规则;在存在多个实现的情况，需要通过条件匹配当前的实现;
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
    /**
     * 默认是空数组
     *
     * @return
     */
    String[] value() default {};
}
