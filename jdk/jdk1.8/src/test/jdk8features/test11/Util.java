package jdk8features.test11;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test11.Util
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/5 23:50
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/5      xuecy           v1.0.0               修改原因
 */
public class Util {

    //    模拟长时间调用
    public static void delay() {
        int delay = 1000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
