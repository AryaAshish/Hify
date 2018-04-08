package com.amsavarthan.hify.ui.extras.Weather.utils.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread manager.
 */

public class ThreadManager {

    private static ThreadManager instance;
    private ExecutorService threadPool;

    private ThreadManager() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
