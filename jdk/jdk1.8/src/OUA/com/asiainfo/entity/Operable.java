package com.asiainfo.entity;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.asiainfo.entity.Operable
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/14 17:10
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/14      xuecy           v1.0.0               修改原因
 */
public interface Operable {
    default int calculate(Operator operator) throws Exception {
        return operator.apply(this);
    }
}
