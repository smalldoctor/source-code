package jdk8features.test8;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test8.ProcessingObject
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/2 20:45
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/2      xuecy           v1.0.0               修改原因
 */
public abstract class ProcessingObject<T> {
    /**
     * 责任链模式：
     * 通过定义一个代表处理对象的抽象类，然后抽象类中定义一个字段引用后继对象；
     * <p>
     * A-》B-》C。。。。 依次关联下个处理对象，形成一个链式的链条
     */
    protected ProcessingObject<T> successor;

    public void setSuccessor(ProcessingObject<T> successor) {
        this.successor = successor;
    }

    //    公用的执行体
    public T execute() throws Exception {
        T r = handle();
        if (successor != null) {
            return successor.handle();
        }
        return r;
    }

    //    由具体处理类实现具体处理逻辑的抽象方法
    public abstract T handle() throws Exception;
}
