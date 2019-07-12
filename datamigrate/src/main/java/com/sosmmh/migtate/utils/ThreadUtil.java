package com.sosmmh.migtate.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 15:26
 */
public class ThreadUtil {

//    public static final int AVAILABLE_THREAD = Runtime.getRuntime().availableProcessors();
    public static final int AVAILABLE_THREAD = 2;

    public static final int THREAD_KEEP_ALIVE_TIME = 5;

    public static ThreadPoolExecutor THREAD_POOL;

    public static ThreadPoolExecutor getThreadPool() {
        if (THREAD_POOL == null) {
            THREAD_POOL = new ThreadPoolExecutor(AVAILABLE_THREAD, AVAILABLE_THREAD,
                    THREAD_KEEP_ALIVE_TIME, TimeUnit.HOURS, new LinkedBlockingQueue<>());
        }
        return THREAD_POOL;
    }

    public static void shutdown() {
        ThreadUtil.getThreadPool().shutdown();
    }

}
