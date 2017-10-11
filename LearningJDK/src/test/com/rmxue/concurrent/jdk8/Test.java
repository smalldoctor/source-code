package com.rmxue.concurrent.jdk8;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.lang.reflect.Array;
import java.util.*;

public class Test {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @org.junit.Test
    public void testLambda() {
        /**
         * Lambda是需要具体实现的函数体
         */
        List<String> word = Arrays.asList(new String[]{"e", "a", "c", "d"});
        // 修改的是原有的List
        Collections.sort(word, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println(word);

        List<String> word2 = Arrays.asList(new String[]{"e", "a", "c", "d"});
        // 修改的是原有的List
        Collections.sort(word2, (String a, String b) -> {
            return a.compareTo(b);
        });
        System.out.println(word2);

        List<String> word3 = Arrays.asList(new String[]{"e", "a", "c", "d"});
        // 修改的是原有的List
        Collections.sort(word3, (String a, String b) -> a.compareTo(b));
        System.out.println(word3);

        List<String> word4 = Arrays.asList(new String[]{"e", "a", "c", "d"});
        // 修改的是原有的List
        // lambda进行自动的类型推导
        Collections.sort(word4, (a, b) -> a.compareTo(b));
        System.out.println(word4);

        Converter<String, Integer> converter = from -> Integer.valueOf(from);
        System.out.println(converter.converter("123123"));

        // :: 用于引用方法
        Converter<String, Integer> c2 = Integer::valueOf;
        System.out.println(converter.converter("12"));

        String str = new String("sffsdfsdfsd");
        Converter<String, Integer> c3 = str::indexOf;
        System.out.println(c3.converter("sf"));

        // :: 引用构造函数
        PersonFactory personFactory = Person::new;
        Person person = personFactory.createPerson("a", "b");

        // 访问局部变量，局部变量如果未声明为final，在被lambda访问之后，会被默认隐性的当作final，不能再次修改其值
        int num = 1;
        Converter<Integer, String> stringConverter = from -> String.valueOf(num + from);
        System.out.println(stringConverter.converter(2));
        // 此处num再次赋值则不合法
//        num = 2;
    }

    @org.junit.Test
    public void testJDK8() {
        Formula formula = new Formula() {
            @Override
            public double calculate(int a) {
                return sqrt(a * 100);
            }
        };
        formula.sqrt(16);
        formula.calculate(100);
    }

    @org.junit.Test(expected = NullPointerException.class)
    public void testException() {

    }

    @org.junit.Test(timeout = 200)
    public void testTimeOut() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
