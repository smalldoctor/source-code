package jdk8inaction.chap4;

import java.util.function.Function;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap4.Main
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/21 11:07
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/21      xuecy           v1.0.0               修改原因
 */
public class Main {
    public static void main(String[] args) {
        // andThen 相当于函数的嵌套，先执行A，再执行B，再执行C。。。。。
        Function<Integer, Integer> f = x -> x + 1;
        Function<Integer, Integer> g = x -> x * 2;
        Function<Integer, Integer> h = f.andThen(g);
        System.out.println(h.apply(1));

//--------等价于
        Function<Integer, Integer> f1 = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer + 1;
            }
        };
        Function<Integer, Integer> g1 = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer * 2;
            }
        };
        Function<Integer, Integer> h1 = f1.andThen(g1);
        System.out.println(h1.apply(1));
    }
}
