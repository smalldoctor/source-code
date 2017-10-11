package com.rmxue.concurrent.jdk8;

@FunctionalInterface
public interface Converter<F, T> {
    T converter(F from);
}
