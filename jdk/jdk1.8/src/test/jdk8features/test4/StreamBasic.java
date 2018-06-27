package jdk8features.test4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap4.StreamBasic
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/21 01:33
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/21      xuecy           v1.0.0               修改原因
 */
public class StreamBasic {
    public static void main(String[] args) {
        getLowCaloricDishesNamesInJava8(Dish.menu).forEach(System.out::println);
    }

    public static List<String> getLowCaloricDishesNamesInJava8(List<Dish> dishes) {
//        lambda 表达式其实相当于定义一个具体的实现，是一个定义
        List list = dishes.stream()
                // 声明式的，通过比较直观的表达，
                .filter(dish -> {
                    System.out.println("filter :" + dish.getName());
                    return dish.getCalories() > 300;
                })
                /**
                 * 有没有sorted，流的处理方式不同：
                 * 1. 如果没有sorted，即挨个元素调用流操作流水，filter，map，limit，进行短路操作；
                 * 2. 如果有sorted，会进行将sort之前的操作和之后的操作分为两块，即sort之前的操作挨个元素调用流操作流水，filter，filter2；然后sort操作；
                 * 最后sort之后的操作挨个元素调用流操作流水，map，limit，进行短路操作；
                 * */
                .sorted(Comparator.comparing(Dish::getCalories))
                .filter(dish -> {
                    System.out.println("filter2 :" + dish.getName());
                    return dish.getCalories() > 450;
                })
                .map(dish -> {
                    System.out.println("map :" + dish.getName());
                    return dish.getName();
                })
                .limit(2)
                // 如果流只有中间操作，没有终端操作，是不会触发流的处理;即注释collect，则不会引发如上流的处理
                .collect(Collectors.toList());
        System.out.println(list.toArray());
        return new ArrayList<>();
    }
}
