package jdk8features.test4;

import java.util.function.Function;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap4.Currying
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/21 15:39
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/21      xuecy           v1.0.0               修改原因
 */
public class Currying {
    public static void main(String[] args) {
        /*
         * 函数柯里化，一个多个参数的函数，转换为一个新的函数可以接受单个参数，然后返回一个接受其他参数的函数；
         * 函数的真正实现通常应该放在，最里面的函数，因为最里面的函数可以拿到所有的参数
         * */
        Function<Integer, Function<Integer, Function<Integer, Integer>>> curring = x -> y -> z -> (x + y) * z;
        Function curring1 = curring.apply(1);
    }
}
