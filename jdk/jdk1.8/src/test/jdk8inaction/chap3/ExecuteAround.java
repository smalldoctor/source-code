package jdk8inaction.chap3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap3.ExecuteAround
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/5/9 15:58
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/5/9      xuecy           v1.0.0               修改原因
 */
public class ExecuteAround {

    public static void main(String[] args) throws Exception {
        String oneLine = processFile((BufferedReader br) -> br.readLine());
        System.out.println(oneLine);
        String twoLine = processFile((BufferedReader br) -> br.readLine() + br.readLine());
        System.out.println(twoLine);
        List<String> strs = new ArrayList<>();
        strs.add("a");
        strs.add("bb");
        strs.add("ccc");
        System.out.println(map(strs, (String str) -> str.length()).toString());
        System.out.println(map(Arrays.asList("lambda", "a", "bb"), (String str) -> str.length()));

        // 捕获Lambda，即捕获作用域外的变量
        BufferedReaderProcessor brp = bufferedReader -> {
            String outOneLine = oneLine;
            return null;
        };
//        oneLine = "";
    }

    public static String processFile(BufferedReaderProcessor processor) throws IOException {
        // 资源主动关闭
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/xuechunyang/Thinkings/Workspace/source-code/jdk/jdk1.8/src/main/JDK.png"))) {
            return processor.process(br);
        }
    }

    @FunctionalInterface
    public interface BufferedReaderProcessor {
        public String process(BufferedReader bufferedReader) throws IOException;
    }

    public static <T, R> List<R> map(List<T> input, Function<T, R> map) {
        List<R> result = new ArrayList<>();
        for (T s : input) {
            result.add(map.apply(s));
        }
        return result;
    }

}
