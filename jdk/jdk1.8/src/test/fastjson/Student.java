package fastjson;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: fastjson.Student
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/8/1 15:34
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/8/1      xuecy           v1.0.0               修改原因
 */
public class Student {

    private final String TYPE = "Student";

    private String studentName;
    private Integer studentAge;

    private Integer test2;

    public Integer getTest2() {
        return test2;
    }

    public void setTest2(Integer test2) {
        this.test2 = test2;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getStudentAge() {
        return studentAge;
    }

    public void setStudentAge(Integer studentAge) {
        this.studentAge = studentAge;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentName='" + studentName + '\'' +
                ", studentAge=" + studentAge +
                ", TYPE=" + TYPE +
                '}';
    }
}
