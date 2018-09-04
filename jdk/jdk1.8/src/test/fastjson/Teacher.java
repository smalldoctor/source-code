package fastjson;

import java.util.List;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: fastjson.Teacher
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
public class Teacher {

    private String teacherName;
    private Integer teacherAge;
    private Course course;
    private List<Student> students;
    private Integer test1;

    public Integer getTest1() {
        return test1;
    }

    public void setTest1(Integer test1) {
        this.test1 = test1;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getTeacherAge() {
        return teacherAge;
    }

    public void setTeacherAge(Integer teacherAge) {
        this.teacherAge = teacherAge;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherName='" + teacherName + '\'' +
                ", teacherAge=" + teacherAge +
                ", course=" + course +
                ", students=" + students +
                '}';
    }
}
