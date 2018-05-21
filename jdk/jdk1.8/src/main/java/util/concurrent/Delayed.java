package java.util.concurrent;

/**
 * Mixin：
 * 用于解决多层次继承的问题；通过多重继承的方式，为某个类增加某种能力，而不是通过多层次的继承的方式；
 * 在JAVA中是不能多重继承的，因此通过接口的方式;
 * <p>
 * Delayed用于表示这个对象必须延迟执行；
 */
public interface Delayed extends Comparable<Delayed> {

    /**
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    long getDelay(TimeUnit unit);
}

