package jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: jvm.TestJVM
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-05-07 09:33
 */
public class TestJVM {

  public static void main(String[] args) throws InterruptedException {
//    Thread.sleep(10000);
    final List<AAAAA> aaa = new ArrayList<>(100000);
    final List<BBBBB> bbb = new ArrayList<>(100000);
    final List<CCCCC> ccc = new ArrayList<>(100000);
    final List<DDDDD> ddd = new ArrayList<>(100000);
    for (int i = 0; i < 100000; i++) {
      aaa.add(new AAAAA());
      bbb.add(new BBBBB());
      ccc.add(new CCCCC());
      ddd.add(new DDDDD());
    }
    Thread.sleep(10000);
  }

}
