package basic;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: basic.Singleton
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-09-30 09:27
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-09-30      xuecy           v1.0.0               修改原因
 */
public class Singleton {
    private Singleton() {
    }

    private final static String CONSTANT_STRING = "1";

    static {
        System.out.println("Singleton.<clinit>");
    }

    // 延迟静态变量的初始化；只是先加载LazyHolder，并不会初始化；而初始化是线程安全的，JVM会进行加锁操作
    public static class LazyHolder {
        static final Singleton INSTANCE = new Singleton();

        static {
            System.out.println("LazyHolder.<clinit>");
        }
    }

    public static Object getInstance(boolean flag) {
//        新建数组，不会对元素类进行初始化
        if (flag) return new LazyHolder[2];
        return LazyHolder.INSTANCE;
    }

    public static void main(String[] args) {
        getInstance(true);
        System.out.println("-------");
        getInstance(false);
    }
}
