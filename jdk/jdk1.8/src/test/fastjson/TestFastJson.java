package fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.junit.Test;

import java.util.List;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: fastjson.TestFastJson
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/8/1 15:32
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/8/1      xuecy           v1.0.0               修改原因
 */
public class TestFastJson {
    //json字符串-简单对象型
    private static final String JSON_OBJ_STR = "{\"studentName\":\"lily\",\"studentAge\":12}";

    //json字符串-数组类型
    private static final String JSON_ARRAY_STR = "[{\"studentName\":\"lily\",\"studentAge\":12},{\"studentName\":\"lucy\",\"studentAge\":15}]";

    //复杂格式json字符串
    private static final String COMPLEX_JSON_STR =
            "{\"course2\":{\"courseName\":\"english\",\"code\":1270},\"teacherName\":\"crystall\",\"teacherAge\":27,\"course\":{\"courseName\":\"english\",\"code\":1270},\"students\":[{\"studentName\":\"lily\",\"studentAge\":12},{\"studentName\":\"lucy\",\"studentAge\":15}]}";

    /**
     * 复杂json格式字符串与JavaBean_obj之间的转换
     */
    @Test
    public void testComplexJSONStrToJavaBean() {

        Teacher teacher = JSON.parseObject(COMPLEX_JSON_STR, new TypeReference<Teacher>() {
        });
        //Teacher teacher1 = JSON.parseObject(COMPLEX_JSON_STR, new TypeReference<Teacher>() {});//因为JSONObject继承了JSON，所以这样也是可以的
        String teacherName = teacher.getTeacherName();
        Integer teacherAge = teacher.getTeacherAge();
        Course course = teacher.getCourse();
        List<Student> students = teacher.getStudents();
        System.out.println(teacherName);
        System.out.println(teacherAge);
        System.out.println(teacher);


    }

    @Test
    public void test() {
//        System.out.println(fact(7));;
        char char1[] = {'j', 'a', 'v', 'a'};
        char char2[] = {'j', 'a', 'v', 'a', '1'};
        String s1 = new String(char1);
        String s2 = new String(char2, 0, 4);
        System.out.println(s1.equals(s2));

        System.out.println("=====");

        StringBuffer buffer = new StringBuffer();
        int value = 18;
        do {
            int temp = value & 0x07;
            buffer.append(temp);
        } while ((value >>>= 3) != 0);
        System.out.println(buffer.reverse());

        System.out.println("=====");

        int a = 0;
        outer:
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if(j > i) {
                    continue outer;
                }
                a++;
            }
        }
        System.out.println(a);
    }

    public int fact(int n) {
        if (n <= 0)
            return 1;
        else
            return n * fact(n - 1);
    }

    @Test
    public void test2(){
        System.out.println(~0010101);
        int  a = 25/3%3;
        System.out.println(a);


    }
}
