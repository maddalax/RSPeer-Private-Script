package org.maddev.helpers.cron;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundJob {

    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

    public static void enqueue(Runnable runnable, long period, TimeUnit unit) {
        service.scheduleAtFixedRate(runnable, 0, period, unit);
    }

    public static void dispose() {
        try {
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
