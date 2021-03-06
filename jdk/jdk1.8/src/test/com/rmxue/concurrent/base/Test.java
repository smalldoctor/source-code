package com.rmxue.concurrent.base;

import java.lang.reflect.Method;

public class Test {
    @org.junit.Test
    public void testException() {
        /*RuntimeException与Exception的区别
        * 处理策略的不一样：
        * 1. RuntimeException在编译期可以不进行处理
        * 2. Exception在编译期则必须进行处理，cache或者throws；
        *
        * */
//        throw new RuntimeException();

        /*try {
            throw new NullPointerException();
        } catch (Exception e) {
            System.out.println("catch....");
        } finally {
            System.out.println("finally .....");
        }

        try {
            // 没有catch，一样会被抛出，只是finally起到最后清理资源的作用
            throw new NullPointerException();
        } finally {
            System.out.println("finally .....");
        }*/

       test();
    }

    public Object test(){
        try {
            /**
             * return 后面的语句抛出异常，因此不会退出方法
             */
            return 1/0;
        }catch (Throwable e){
            System.out.println("exception");
        }
        System.out.println("sdsdf.....");
        return 100;
    }

    public void methods(Class<?>[] clazz) {

    }

    /**
     * @param args String数组类型的动态参数,每个参数都是一个String数组
     */
    public static void methodB(String[]... args) {
        System.out.println("methodB.........");
    }

    public static void methodC(String... args) {
        System.out.println("methodC.........");
    }

    public static void main(String[] args) throws NoSuchMethodException {
//        /**
//         * 无法匹配方法
//         */
//        Class<?>[] paraType = new Class<?>[]{};
//        Method method = Test.class.getMethod("methods", paraType);

        /**
         * 如果一个方法是动态参数，那么调用的时候可以通过数组的方式传入相应的参数，JVM会自动将
         * 数组拆分为一个个具体的参数。
         * JVM会自动识别是动态数组参数，还是动态非数组参数，从进行不同的处理
         */
        Class<?>[] paraType = new Class<?>[]{new Class<?>[0].getClass()};
        Method method = Test.class.getMethod("methods", paraType);
        System.out.println(method.toString());
        String[] param = new String[]{};
        // 可以调用
        methodB(param);
        methodC(param);

    }

    @org.junit.Test
    public void testEnum() {
        System.out.println(ProcessCode.Ord_Sec_SerialNewGroup.getProcessName());
    }

}
