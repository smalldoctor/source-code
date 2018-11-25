package ioc;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;
/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: ioc.NodeB
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
@Named("b")
class NodeB implements Node {

    Leaf leaf;

    @Inject
    @Named("a")
    Node a;

    @Inject
    public NodeB(Leaf leaf) {
        this.leaf = leaf;
    }

    @Override
    public String name() {
        if (a == null)
            return String.format("nodeB(%s)", leaf);
        else
            return String.format("nodeBWithA(%s)", leaf);
    }

}
