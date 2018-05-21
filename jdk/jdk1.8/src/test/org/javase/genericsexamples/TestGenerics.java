package org.javase.genericsexamples;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: org.javase.genericsexamples.TestGenerics
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/16 21:52
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/16      xuecy           v1.0.0               修改原因
 */
public class TestGenerics {
    public static void main(String[] args) {
        List<String> stringList = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();
        System.out.println(stringList.getClass());
        System.out.println(integerList.getClass());
        System.out.println(stringList.getClass() == integerList.getClass());
    }
}
