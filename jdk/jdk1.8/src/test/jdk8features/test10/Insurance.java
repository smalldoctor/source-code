package jdk8features.test10;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test10.Insurance
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
public class Insurance {

    /**
     * 引用insurance公司名称时发生NullPointerException，就能非常确定地知道出错的原因，
     * 不需要为其添加null的检查，因为null的检查只会掩盖问题，并未真正地修复问题。
     * insurance公司必须有个名字，所以如果你遇到一个公司没有名称
     * 你需要调查数据是否出了什么问题，而不应该再添加一段代码，将这个问题隐藏。
     */
    private String name;

    public Insurance(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
