package instrumentation.agentmain.attach;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: instrumentation.agentmain.attach.Test
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/30 21:53
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/30      xuecy           v1.0.0               修改原因
 */
public class Test {
    public static void main(String[] args) throws AttachNotSupportedException,
            IOException, AgentLoadException, AgentInitializationException {
        VirtualMachine vm = VirtualMachine.attach("5108");
        vm.loadAgent("/Users/xuechunyang/Thinkings/Workspace/source-code/jdk/out/test/jdk1.8/loadagent.jar");
        System.in.read();
    }
}
