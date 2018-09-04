package fastjson;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: fastjson.Course
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
public class Course {

    private String courseName;
    private Integer code;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseName='" + courseName + '\'' +
                ", code=" + code +
                '}';
    }
}
