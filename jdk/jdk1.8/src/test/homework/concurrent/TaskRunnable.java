package homework.concurrent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: homework.concurrent.TaskRunnable
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/9/4 09:53
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/9/4      xuecy           v1.0.0               修改原因
 */
public class TaskRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TaskRunnable.class);

    public void run() {
        long sTime = System.currentTimeMillis();
        long eTime = 0;
        try {
            Thread.sleep(1000);
            eTime = System.currentTimeMillis();
            logger.info("Thread,id:{},name:{},time:{}", Thread.currentThread().getId(),
                    Thread.currentThread().getName(), eTime - sTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
