package jdk8features.test10;

import java.util.Optional;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test10.Car
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/27 17:22
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/27      xuecy           v1.0.0               修改原因
 */
public class Car {
    /**
     * 1. Optional的存在使用应该思考在业务域中思考是否可能为null;
     *
     * 2. 从语义上，NUll和Optional.empty()可以把它们当作一回事儿，但是实际中它们之间的差别非常大:
     * 如果你尝试解引用一个null，一定会触发NullPointerException，不过使用 Optional.empty()就完全没事儿，
     * 它是Optional类的一个有效对象，多种场景都能调用，非 常有用。
     */
    private Optional<Insurance> insurance;

    public Optional<Insurance> getInsurance() {
        return insurance;
    }
}
