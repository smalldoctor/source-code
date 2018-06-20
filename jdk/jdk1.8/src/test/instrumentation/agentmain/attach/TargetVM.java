package instrumentation.agentmain.attach;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: instrumentation.agentmain.attach.TargetVM
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/30 22:52
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/30      xuecy           v1.0.0               修改原因
 */
public class TargetVM {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            System.out.println("又一次。。。。。。");
            Thread.sleep(1000);
        }
    }
}
