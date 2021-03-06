package com.asiainfo.entity;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.asiainfo.entity.Verifiable
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/14 12:11
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/14      xuecy           v1.0.0               修改原因
 */
public interface Verifiable {
    default boolean verify(Validator validator) throws Exception {
        return validator.validate(this);
    }
}
