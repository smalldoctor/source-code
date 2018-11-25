package ioc;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: ioc.Leaf
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
class Leaf {

    @Inject
    Root root;

    int index;

    static int sequence;

    public Leaf() {
        index = sequence++;
    }

    public String toString() {
        if (root == null)
            return "leaf" + index;
        else
            return "leafwithroot" + index;
    }

}
