package com.teleostnacl.common.java.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ExecutorServiceUtils {
    private static final ExecutorService cachedExecutorService = Executors.newCachedThreadPool();

    public static ExecutorService getCachedExecutorService() {
        return cachedExecutorService;
    }

    /**
     * 使用缓存线程进行执行任务
     *
     * @param runnable 任务
     */
    public static void executeByCached(Runnable runnable) {
        cachedExecutorService.execute(runnable);
    }

    /**
     * 使用线程池进行提交任务并获取返回值
     *
     * @param <T>      返回值类型
     * @param executor 线程池
     * @param task     任务
     * @param supplier 发生错误时的获取返回值的函数
     * @return 使用线程池进行提交任务并获取返回值
     */
    public static <T> T submitByExecutor(ExecutorService executor,
                                         Callable<T> task, Supplier<T> supplier) {
        try {
            return executor.submit(task).get();
        } catch (Exception ignored) {
            return supplier.get();
        }
    }
}
