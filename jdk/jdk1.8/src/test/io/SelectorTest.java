package io;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * Copyright: Copyright (c) 2019 Asiainfo
 *
 * @ClassName: io.SelectorTest
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-04-06 20:02
 *
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2019-04-06      xuecy           v1.0.0               修改原因
 */
public class SelectorTest {

  public static void main(String[] args) throws Exception {
    Selector selector = Selector.open();
//    selector.wakeup();
//    selector.wakeup();
//    selector.select(0);
//    selector.select();

//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          selector.select();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//    }).start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
//          Thread.sleep(1000L);
          selector.wakeup();
          System.out.println("sub wakeup.....");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();

    System.out.println("main sleep.....");
    Thread.sleep(1000L);
    System.out.println("main select.....");
    selector.select();
  }

}
