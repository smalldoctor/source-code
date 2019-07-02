package extension;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: extension.Product
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-12-08 14:29
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-12-08      xuecy           v1.0.0               修改原因
 */
public @interface Product {
    //    业务实现
    String code() default "";

    //    扩展点的其他条件
    String name() default "";
}
