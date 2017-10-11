package com.rmxue.concurrent.unsafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: xuecy
 * @Date: 2017/5/11
 * @RealUser: Chunyang Xue
 * @Time: 20:45
 * @Package: com.rmxue.concurrent
 * @Email: 15312408287@163.com
 */
public class TestUnsafe {

//    Person person;

    AtomicReference<Person> person = new AtomicReference<>();

    static class Person {
        int age;
        String name;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

//    private static final Unsafe UNSAFE;
//    private static final long AGE;
//    private static final long NAME;
//    private static final long PERSON;
//
//    static {
//        UNSAFE = Unsafe.getUnsafe();
//        try {
//            AGE = UNSAFE.objectFieldOffset(Person.class.getDeclaredField("age"));
//            NAME = UNSAFE.objectFieldOffset(Person.class.getDeclaredField("name"));
//            PERSON = UNSAFE.objectFieldOffset(TestUnsafe.class.getDeclaredField("person"));
//        } catch (NoSuchFieldException e) {
//            throw new Error(e);
//        }
//    }

    public static void main(String[] args) {
        TestUnsafe tu1 = new TestUnsafe();
        Person person3 = new Person();
        person3.setAge(1);
        person3.setName("p1");
        tu1.person.set(person3);

        Person person = new Person();
        person.setAge(1);
        person.setName("p1");

        Person person2 = new Person();
        person2.setAge(2);
        person2.setName("p2");

        boolean b = tu1.person.compareAndSet(person,person2);
        System.out.println(b);
        System.out.println(tu1.person.get().getName());
    }
}
