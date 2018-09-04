package com.asiainfo.entity;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.asiainfo.entity.Test
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/7/14 12:49
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/7/14      xuecy           v1.0.0               修改原因
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Customer customer = new Customer();
        customer.verify(verifiableObject -> "111".equals("b"));
        customer.verify(new Validator() {
            @Override
            public boolean validate(Verifiable verifiableObject) throws Exception {
                return false;
            }
        });
    }
}
