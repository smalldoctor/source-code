package ioc;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: ioc.NodeA
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-10-25 23:32
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-10-25      xuecy           v1.0.0               修改原因
 */
@Singleton
@Named("a")
class NodeA implements Node {

    @Inject
    Leaf leaf;

    @Inject
    @Named("b")
    Node b;

    @Override
    public String name() {
        if (b == null)
            return String.format("nodeA(%s)", leaf);
        else
            return String.format("nodeAWithB(%s)", leaf);
    }

}
