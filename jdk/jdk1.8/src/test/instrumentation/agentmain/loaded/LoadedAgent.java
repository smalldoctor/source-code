package instrumentation.agentmain.loaded;

import java.lang.instrument.Instrumentation;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: instrumentation.agentmain.loaded.LoadedAgent
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/30 21:52
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/30      xuecy           v1.0.0               修改原因
 */
public class LoadedAgent {
    @SuppressWarnings("rawtypes")
    public static void agentmain(String args, Instrumentation inst) {
        Class[] classes = inst.getAllLoadedClasses();
        inst.addTransformer(new TestTransformer());
//        for (Class cls : classes) {
//            System.out.println(cls.getName());
//        }
    }
}
