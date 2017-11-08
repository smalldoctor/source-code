package org.apache.coyote;

/**
 * Coyote Request是一个low-level的高效率的请求对象。
 * 大多数字段都是GC-free（不会产生GC的）；
 * 提供get方法，获取信息，类似于hook机制，使用者自己获取信息，而不是主动推送信息；
 * <p>
 * 如何高效率处理：
 * 1. 避免直接转换字符串，直接保存字节；字节到字符串的转换工作在真正使用时再转换；
 * 原理同延迟加载；降级处理；
 * 2. 大部分字段都是GC-Free
 */
public final class Request {
}
