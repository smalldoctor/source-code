package com.rmxue.concurrent.jdk8;

public class lambdaScope {
    static int outStaticNum;
    int outNum;

    public void testScope() {
        /**
         * 在lambda中访问成员变量或者静态变量的时候不需要是final类型
         */
        Converter<String, String> converter = from -> {
            outNum = 3;
            return outNum + "";
        };
        Converter<String, String> converter1 = from -> {
            outStaticNum = 4;
            return outStaticNum + "";
        };
        outNum = 4;
    }
}
