package classloader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import javax.sound.midi.Soundbank;

/**
 * Copyright: Copyright (c) 2019 Asiainfo
 *
 * @ClassName: classloader.TestClassPath
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-04-10 11:59
 *
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2019-04-10      xuecy           v1.0.0               修改原因
 */
public class TestClassPath {

  public static void main(String[] args) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    System.out.println("---------------------------"+loader);
    printClasspath((URLClassLoader) loader);
    System.out.println("---------------------------"+loader.getParent());
    printClasspath((URLClassLoader) loader.getParent());

  }

  private static void printClasspath(URLClassLoader loader) {
    URL repositories[] = loader.getURLs();
    for (int i = 0; i < repositories.length; i++) {
      String repository = repositories[i].toString();
      if (repository.startsWith("file://")) {
      } else if (repository.startsWith("file:"))
        System.out.println(repository);
      else if (repository.startsWith("jndi:"))
        System.out.println(repository);

      else
        continue;
      if (repository == null)
        continue;
    }
  }

}
