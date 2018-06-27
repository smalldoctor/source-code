package jdk8features.test7;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: jdk8inaction.chap7.ForkJoinSumCalculator
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/6/26 15:54
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/6/26      xuecy           v1.0.0               修改原因
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    public static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    private final long[] numbers;
    private final int start;
    private final int end;

    //    任务拆分的阀值
    public static final int THRESHOLD = 10_000;

    //    用于创建主任务
    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    //    用于创建子任务
    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            // 最小任务，则计算
            return computeSequentially();
        } else {
            //            非最小任务，则拆分任务
            ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);
            leftTask.fork();
            // fork 使用ForkJoinPool的另一个线程执行
            ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);
            long rightResult = rightTask.compute();
            long leftResult = leftTask.join();
            return rightResult + leftResult;
        }
    }

    public long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static long forkJoinSum(long number) {
        long[] numbers = LongStream.rangeClosed(1, number).toArray();
        ForkJoinSumCalculator task = new ForkJoinSumCalculator(numbers);
        return FORK_JOIN_POOL.invoke(task);
    }
}
