package jdk8features.test11;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

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

    private final static Random RANDOM = new Random(0);
    private final static DecimalFormat formatter = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    //    模拟长时间调用
    public static void delay() {
        int delay = 1000;
//        动态随机延迟
//        int delay = 500 + RANDOM.nextInt(2000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static double format(double number) {
        // DecimalFormat非线程安全的
        synchronized (formatter) {
            return new Double(formatter.format(number));
        }
    }
}
