package performance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: performance.RegTest
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-06-11 20:11
 */
public class RegTest {

//  public static void main( String[] args )
//  {
//    String text = "<input high=\"20\" weight=\"70\">test</input>";
//    String reg="(<input.*?>)(.*?)(</input>)";
//    Pattern p = Pattern.compile(reg);
//    Matcher m = p.matcher(text);
//    while(m.find()) {
//      System.out.println(m.group(0));// 整个匹配到的内容
//      System.out.println(m.group(1));//(<input.*?>)
//      System.out.println(m.group(2));//(.*?)
//      System.out.println(m.group(3));//(</input>)
//    }
//  }

  public static void main( String[] args )
  {
    String text = "<input high=\"20\" weight=\"70\">test</input>";
    String reg="(?:<input.*?>)(.*?)(?:</input>)";
    Pattern p = Pattern.compile(reg);
    Matcher m = p.matcher(text);
    while(m.find()) {
      System.out.println(m.group(0));// 整个匹配到的内容
      System.out.println(m.group(1));//(.*?)
    }
  }

}
