package com.rmxue.concurrent.testdeadlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.testdeadlock.TestDeadLock
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/27 10:49
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/27      xuecy           v1.0.0               修改原因
 */
public class TestDeadLock {
    public static void main(String[] args) {
        int num = 4;
        Resource[] res = new Resource[num];
        ExecutorService exec = Executors.newFixedThreadPool(num);
        for (int i = 0; i < num; i++) {
            res[i] = new Resource(i);
        }
        for (int i = 0; i < num; i++) {
            exec.execute(new DeadLockExp(res[i], res[(i + 1) % num]));
        }
        exec.shutdown();
    }
}
