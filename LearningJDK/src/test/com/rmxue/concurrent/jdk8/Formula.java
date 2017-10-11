package com.rmxue.concurrent.jdk8;

public interface Formula {
    double calculate(int a);

    /**
     * JDK8支持接口有默认实现
     *
     * @param a
     * @return
     */
    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}
