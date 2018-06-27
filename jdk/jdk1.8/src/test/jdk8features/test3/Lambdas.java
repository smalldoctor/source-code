package jdk8features.test3;

import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Callable;

public class Lambdas {
    public static void main(String... args) {

        // Simple example
        Runnable r = () -> System.out.println("Hello!");
        r.run();

//        同样的Lambda，不同的函数式接口
        Callable<Integer> callable = () -> 42;
        PrivilegedAction<Integer> privilegedAction = () -> 42;

        List<Apple> inventory = Arrays.asList(new Apple(80, "green"), new Apple(155, "green"), new Apple(120, "red"));

        List<Apple> greenApples = filter(inventory, (Apple a) -> "green".equals(a.getColor()));
//        ===
        greenApples = filter(inventory, new ApplePredicate() {
            @Override
            public boolean test(Apple a) {

                 return  "green".equals(a.getColor());
//                return false;
            }
        });
        System.out.println(greenApples);


        Comparator<Apple> c = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());

        inventory.sort(c);
        System.out.println(inventory);
    }

    interface ApplePredicate {
        public boolean test(Apple a);
    }

    public static List<Apple> filter(List<Apple> inventory, ApplePredicate p) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple: inventory) {
            if (p.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }

    public static class Apple {
        private int weight = 0;
        private String color = "";

        public Apple(int weight, String color) {
            this.weight = weight;
            this.color = color;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String toString() {
            return "Apple{" +
                    "color='" + color + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }

}