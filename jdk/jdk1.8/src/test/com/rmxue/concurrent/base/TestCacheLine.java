package com.rmxue.concurrent.base;

import org.junit.Test;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.base.TestCacheLine
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/25 17:36
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/25      xuecy           v1.0.0               修改原因
 */

public class TestCacheLine {
    static final int LINE_NUM = 1024;
    static final int COLUM_NUM = 1024;

    @Test
    public  void cacheline1() {

        long[][] array = new long[LINE_NUM][COLUM_NUM];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < COLUM_NUM; ++i) {
            for (int j = 0; j < LINE_NUM; ++j) {
                array[j][i] = i * 2 + j;
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("no cache time:" + (endTime - startTime));

    }

    @Test
    public  void cacheline2() {

        long[][] array = new long[LINE_NUM][COLUM_NUM];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LINE_NUM; ++i) {
            for (int j = 0; j < COLUM_NUM; ++j) {
                array[i][j] = i * 2 + j;
            }
        }
        long endTime = System.currentTimeMillis();
        long cacheTime = endTime - startTime;
        System.out.println("cache time:" + cacheTime);

    }


}
