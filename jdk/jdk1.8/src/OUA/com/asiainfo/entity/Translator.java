package com.asiainfo.entity;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.asiainfo.entity.Translator
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/14 13:16
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/14      xuecy           v1.0.0               修改原因
 */
public interface Translator<S, R> {
    R translate(S s);
}
