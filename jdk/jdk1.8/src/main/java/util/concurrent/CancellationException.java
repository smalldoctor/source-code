package java.util.concurrent;

/**
 * 如果因为取消而无法获取结果，则抛出CancellationException；
 * 这是运行时异常
 */
public class CancellationException extends IllegalStateException {
    public CancellationException(String message) {
        super(message);
    }

    public CancellationException() {
    }
}
