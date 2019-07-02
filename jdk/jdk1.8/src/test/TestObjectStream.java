import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName: PACKAGE_NAME.TestObjectStream
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-04-28 11:13
 */
public class TestObjectStream {

  public Map map;

  @Before
  public void setUp() {
    map = new HashMap<>();
    map.put("a", "a");
    map.put("b", "b");
  }

  @Test
  public void testObjectStream() throws IOException, ClassNotFoundException {
    SerializeUtils serializeUtils = new SerializeUtils();
    String str = serializeUtils.serialize(map);
    Object object = serializeUtils.serializeToObject(str);
    System.out.println(object);
  }

  public class SerializeUtils {

    /**
     * 序列化对象
     */
    public String serialize(Object obj) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream;
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(obj);
      String string = byteArrayOutputStream.toString("ISO-8859-1");
      objectOutputStream.close();
      byteArrayOutputStream.close();
      return string;
    }

    /**
     * 反序列化对象
     */
    public Object serializeToObject(String str) throws IOException, ClassNotFoundException {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
          str.getBytes("ISO-8859-1"));
      ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
      Object object = objectInputStream.readObject();
      objectInputStream.close();
      byteArrayInputStream.close();
      return object;
    }

  }
}
