package io.netty.util.internal;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author xuecy
 * @Date: 15/11/11
 * @RealUser:Chunyang Xue
 * @Time: 23:35
 * @Package:io.netty.util.internal
 * @Email:xuecy@asiainfo.com
 */

/**
 * （不期望被重写，对于独立的特定实现的工具类）
 * 基于线程独立的随机数生成器。ThreadLocalRandom会使用seed进行初始化。
 * 使用线程独立ThreadLocalRandom比共享Random降低开销和减少竞争。
 * TreadLocalRandom主要用于线程池的多任务处理。
 */
public final class ThreadLocalRandom extends Random {
    private final static InternalLogger logger = InternalLoggerFactory.getInstance(ThreadLocalRandom.class);

    private static AtomicLong seedUniquifier = new AtomicLong();

    // volatile 保证初始化即可见 多线程的可见
    private static volatile long initialSeedUniquifier =
            SystemPropertyUtil.getLong("io.netty.initialSeedUniquifier", 0);

    // final
    private final static Thread seedGeneratorThread;
    // final 待研究分析BlockingQueue 为什么使用阻塞队列
    private final static BlockingQueue<byte[]> seedQueue;
    // final
    private final static long seedGeneratorStartTime;
    private static volatile long seedGeneratorEndTime;

    static {
        if (initialSeedUniquifier == 0) {
            /**
             * 使用单独的线程处理生成种子，避免使用线程阻塞
             */
            seedGeneratorThread = new Thread("initialSeedUniquifierGenerator") {
                @Override
                public void run() {
                    // 待研究 SecureRandom
                    final SecureRandom secureRandom = new SecureRandom();
                    // 生成指定字节数的seed
                    final byte[] seed = secureRandom.generateSeed(8);
                    // 高精度的虚拟机的时间串，比较两个时间应该是相减>0，而不是直接比较这两个值，防止溢出
                    seedGeneratorEndTime = System.nanoTime();
                    // 初始化要放在线程的外面
//                    seedQueue = new LinkedBlockingQueue<byte[]>();
                    seedQueue.add(seed);
                }
            };

            seedGeneratorThread.setDaemon(true);
            seedGeneratorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    logger.debug("An exception has been raised by {}", t.getName(), e);
                }
            });
            seedQueue = new LinkedBlockingQueue<byte[]>();
            seedGeneratorStartTime = System.nanoTime();
            seedGeneratorThread.start();
        } else {
            // 如果在启动的时候提供io.netty.initialSeedUniquifier系统级种子参数，则无须生成种子
            seedGeneratorThread = null;
            seedQueue = null;
            seedGeneratorStartTime = 0;
        }
    }

    /**
     * 设置seed
     */
    public static void setInitialSeedUniquifier(long initialSeedUniquifier) {
        ThreadLocalRandom.initialSeedUniquifier = initialSeedUniquifier;
    }

    public static long getInitialSeedUniquifier() {
        //
        long initialSeedUniquifier = ThreadLocalRandom.initialSeedUniquifier;
        // 如果initialSeedUniquifier 大于0 则直接使用
        if (initialSeedUniquifier > 0) {
            return initialSeedUniquifier;
        }

        // 线程同步
        synchronized (ThreadLocalRandom.class) {
            // 再次校验是否已经初始化
            initialSeedUniquifier = ThreadLocalRandom.initialSeedUniquifier;
            if (initialSeedUniquifier > 0) {
                return initialSeedUniquifier;
            }

            // 在超时时间内获取seed
            final long timeoutSeconds = 3;
            // 1. seedGeneratorStartTime 是什么值，是什么时候的值；2. TimeUnit待研究
            final long deadLine = seedGeneratorStartTime + TimeUnit.SECONDS.toNanos(timeoutSeconds);
            boolean interrupted = false;
            for (; ; ) {
                final long waitTime = deadLine - System.nanoTime();
                try {
                    final byte[] seed;
                    if (waitTime < 0) {
                        // 从队列中获取队列头并从队列中移出
                        seed = seedQueue.poll();
                    } else {
                        // 从队列中获取队列头并从队列中移出，如果没有可用值等待指定时间，如果等待过程中Interrupted则抛出InterruptedException
                        seed = seedQueue.poll(waitTime, TimeUnit.NANOSECONDS);
                    }
                    if (seed != null) {
                        initialSeedUniquifier = ((long) seed[0] & 0xff) << 56 |
                                ((long) seed[1] & 0xff) << 48 |
                                ((long) seed[2] & 0xff) << 40 |
                                ((long) seed[3] & 0xff) << 32 |
                                ((long) seed[4] & 0xff) << 24 |
                                ((long) seed[5] & 0xff) << 16 |
                                ((long) seed[6] & 0xff) << 8 |
                                (long) seed[7] & 0xff;
                        break;
                    }
                } catch (InterruptedException interruptedException) {
                    interrupted = true;
                    logger.warn("Failed to generate a seed from SecureRandom due to an InterruptedException. ");
                    break;
                }
                if (waitTime <= 0) {
                    seedGeneratorThread.interrupt();
                    logger.warn(
                            "Failed to generate a seed from SecureRandom within {} seconds. " +
                                    "Not enough entropy?", timeoutSeconds
                    );
                    break;
                }
            }

            // Just in case the initialSeedUniquifier is zero or some other constant
            initialSeedUniquifier ^= 0x3255ecdc33bae119L; // just a meaningless random number
            initialSeedUniquifier ^= Long.reverse(System.nanoTime());

            ThreadLocalRandom.initialSeedUniquifier = initialSeedUniquifier;

            if (interrupted) {
                // Restore the interrupt status because we don't know how to/don't need to handle it here.
                Thread.currentThread().interrupt();

                // Interrupt the generator thread if it's still running,
                // in the hope that the SecureRandom provider raises an exception on interruption.
                seedGeneratorThread.interrupt();
            }

            if (seedGeneratorEndTime == 0) {
                seedGeneratorEndTime = System.nanoTime();
            }

            return initialSeedUniquifier;

        }
    }

    public static long newSeed() {
        for (; ; ) {
            final long current = seedUniquifier.get();
            final long actualCurrent = current != 0 ? current : getInitialSeedUniquifier();

            final long next = actualCurrent * 181783497276652981L;

            if (seedUniquifier.compareAndSet(current, actualCurrent)) {
                if (current == 0 && logger.isDebugEnabled()) {
                    if (seedGeneratorEndTime != 0) {
                        logger.debug(String.format(
                                "-Dio.netty.initialSeedUniquifier: 0x%016x (took %d ms)",
                                actualCurrent,
                                TimeUnit.NANOSECONDS.toMillis(seedGeneratorEndTime - seedGeneratorStartTime)));
                    } else {
                        logger.debug(String.format("-Dio.netty.initialSeedUniquifier: 0x%016x", actualCurrent));
                    }
                }
                return next ^ System.nanoTime();
            }
        }
    }

    /**
     * 此Random只能在new时初始化，不能单独调用setSeed方法，所以用此boolean
     * 表示initialized
     */
    boolean initialized;

    // 为什么是这个值
    private final long multiplier = 0x5DEECE66DL;
    private final long mask = (1L << 48) - 1;

    ThreadLocalRandom() {
        /**
         * Random构造器：
         * 使用给定的seed作为初始化seed生成一个generator；seed是伪随机数generator（next方法）的内部状态的初始化值
         * new Random(seed) 等价于
         * Random rnd = new Random();
         * rnd.setSeed(seed);
         */
        super(newSeed());
        initialized = true;
    }

    private long rnd;

    /*
    * 此处重写setSeed为了屏蔽父类的setSeed功能
    * */
    public void setSeed(long seed) {
        if (initialized) {
            throw new UnsupportedOperationException("");
        }
        rnd = (seed ^ multiplier) & mask;
    }

    public static void main(String[] args) {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] seed = secureRandom.generateSeed(8);
        System.out.println(String.valueOf(seed));
    }
}
