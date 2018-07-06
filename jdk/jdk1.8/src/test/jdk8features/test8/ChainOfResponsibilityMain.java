package jdk8features.test8;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8features.test8.ChainOfResponsibilityMain
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/3 17:33
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/3      xuecy           v1.0.0               修改原因
 */
public class ChainOfResponsibilityMain {
    public static void main(String[] args) {
        UnaryOperator<String> headerProcessing =
                (String text) -> "From Raoul, Mario and Alan: " + text;
        UnaryOperator<String> spellCheckerProcessing =
                (String text) -> text.replaceAll("labda", "lambda");
//        addThen 通过封装形成链式调用
        Function<String, String> pipeline = headerProcessing.andThen(spellCheckerProcessing).andThen(headerProcessing);
//        addThen A.andThen(B).addThen(C) 先执行A，在执行B,最后执行C=》C(B(A()))
        pipeline.andThen(spellCheckerProcessing);
    }
}
