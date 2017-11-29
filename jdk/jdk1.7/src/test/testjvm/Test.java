package testjvm;

public class Test {
    @org.junit.Test
    public void test() {

        Object[] arrays = new Object[10];
        boolean flag = false;

     /*   // 编译失败
        breakpoint:
        if (flag) System.out.println();*/
        breakpoint:
        for (int i = 0; i < arrays.length; i++) {
            Object array = arrays[i];
            if (i < 5) {
                System.out.println(i);
            } else {
                break breakpoint;
            }
        }

     /*
       // 编译失败
       breakpoint:
        arrays = new Object[1];*/
    }
}
