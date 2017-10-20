package io.netty.util.internal;

import java.lang.annotation.*;

/**
 * 注释性的注解，标识接口是不稳定的
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.TYPE
})
@Documented
public @interface UnstableApi {
}
