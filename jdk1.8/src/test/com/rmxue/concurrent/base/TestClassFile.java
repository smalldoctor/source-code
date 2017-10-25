package com.rmxue.concurrent.base;

public class TestClassFile {
    private volatile String a;

    public static void main(String[] args) {
        TestClassFile testClassFile = new TestClassFile();
        for (int i = 0; i < 100000; i++) {
            testClassFile.test();
        }
    }

    public void test() {
        a = "test class file";
        System.out.println(a);
    }
}
