package com.rmxue.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright: Copyright (c) 2019 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.ConcurrentHashMapDemo
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-03-25 11:05
 *
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2019-03-25      xuecy           v1.0.0               修改原因
 */
public class ConcurrentHashMapDemo {

  private Map<Integer, Integer> cache = new ConcurrentHashMap<>(15);

  public static void main(String[] args) {
//    ConcurrentHashMapDemo ch = new ConcurrentHashMapDemo();
//    System.out.println(ch.fibonaacci(80));

    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
//    map.computeIfAbsent("AaAa",
//        key -> map.
//            computeIfAbsent("BBBB", key2 -> "value"));
    int h = "AaAa".hashCode();
    System.out.println( (h ^ (h >>> 16)) & 0x7fffffff);
    h = "BBBB".hashCode();
    System.out.println( (h ^ (h >>> 16)) & 0x7fffffff);
  }

  public int fibonaacci(Integer i) {
    if (i == 0 || i == 1) {
      return i;
    }

    return cache.computeIfAbsent(i, (key) -> {
      System.out.println("fibonaacci : " + key);
      return fibonaacci(key - 1) + fibonaacci(key - 2);
    });

  }
}
