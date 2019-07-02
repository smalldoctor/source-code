package basic;

import java.util.Vector;

/**
 * Copyright: Copyright (c) 2019 Asiainfo
 *
 * @ClassName: basic.OnStackTest
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-03-27 09:46
 *
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2019-03-27      xuecy           v1.0.0               修改原因
 */
public class OnStackTest {

  public static void main(String[] args) throws InterruptedException {
    Vector<byte[]> v = new Vector<byte[]>();

    long b = System.currentTimeMillis();

    for (int i = 0; i < 100; i++) {

      v.add(new byte[10 * 1024 * 1024]);
    }
  }
}
