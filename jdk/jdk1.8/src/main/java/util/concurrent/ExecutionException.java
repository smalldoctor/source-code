package java.util.concurrent;

/**
 * 当尝试获取任务结果失败时抛出的异常；
 * 通过getCause方法进行检查；
 */
public class ExecutionException extends Exception {
    private static final long serialVersionUID = 7830266012832686185L;

    /**
     * 构建无初始化信息的异常；
     * Cause可以通过initCause方法进行初始化；
     */
    public ExecutionException() {
    }

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Message信息通过
     * cause == null ? null : cause.toString()
     * 获取
     *
     * @param cause
     */
    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
