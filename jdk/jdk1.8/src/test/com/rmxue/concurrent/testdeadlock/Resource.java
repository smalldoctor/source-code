package com.rmxue.concurrent.testdeadlock;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.testdeadlock.Resource
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/27 10:45
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/27      xuecy           v1.0.0               修改原因
 */
public class Resource {
    private int id;
    public Resource(int id) { this.id = id; }
    public int getId() { return id; }
    public String toString(){ return ""+id; };
}
