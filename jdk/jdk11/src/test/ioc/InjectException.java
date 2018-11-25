package ioc;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: InjectException
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-10-25 22:51
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-10-25      xuecy           v1.0.0               修改原因
 */
public class InjectException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public InjectException() {
        super();
    }

    public InjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public InjectException(String message) {
        super(message);
    }

    public InjectException(Throwable cause) {
        super(cause);
    }
}
