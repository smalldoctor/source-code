package jdk8features.test10;

import java.util.Optional;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test10.Person
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/27 17:26
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/27      xuecy           v1.0.0               修改原因
 */
public class Person {
    /**
     * 在声明变量时使用的是Optional<Car>类型，而不是Car类型，这句声明非常清楚地表明了这
     * 里发生变量缺失是允许的。与此相反，使用Car这样的类型，可能将变量赋值为null，这意味
     * 着你需要独立面对这些，你只能依赖你对业务模型的理解，判断一个null是否属于该变量的有效范畴。
     * <p>
     * 定义Optional可以使用者能够更清楚,使得的模型的语义更丰富
     */
//    private Car car;
    private Optional<Car> car;

    public Optional<Car> getCar() {
        return car;
    }
}
