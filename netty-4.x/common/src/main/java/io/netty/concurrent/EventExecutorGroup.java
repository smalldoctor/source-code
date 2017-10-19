package io.netty.concurrent;

import java.util.concurrent.ScheduledExecutorService;

/**
 * EventExecutorGroup承担两个角色：
 * 1. 通过next方法提供EventExecutor
 * 2. 负责处理EventExecutor的生命周期
 */
public interface EventExecutorGroup extends ScheduledExecutorService, Iterable<EventExecutor> {
}
