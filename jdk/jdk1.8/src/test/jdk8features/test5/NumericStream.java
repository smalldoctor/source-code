package jdk8features.test5;

import jdk8features.test4.Dish;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap5.NumericStream
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/21 16:34
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/21      xuecy           v1.0.0               修改原因
 */
public class NumericStream {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(3, 4, 5, 1, 2);
        numbers.stream()
                .filter(integer -> integer % 2 == 0)
                .distinct()
                .forEach(System.out::println);

//        limit 和 skip互补，limit是前几个，skip是跳过前几个
        Dish.menu.stream()
                .filter(dish -> dish.getCalories() > 400)
                .skip(2)
                .collect(Collectors.toList());
        List<String> words = Arrays.asList("Java 8", "Lambdas", "In", "Action");
        System.out.println(words.stream()
                .map(String::length)
                .collect(Collectors.toList()));
    }
}
