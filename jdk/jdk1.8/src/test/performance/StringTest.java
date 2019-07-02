package performance;

/**
 * @ClassName: performance.String
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-06-11 19:42
 */
public class StringTest {

  public static void main(String[] args) {

    String str1 = "abc";
    String str2 = new String("abc");
    String str3 = str2.intern();
    System.out.println(str1 == str2);
    System.out.println(str2 == str3);
    System.out.println(str1 == str3);

  }
}
