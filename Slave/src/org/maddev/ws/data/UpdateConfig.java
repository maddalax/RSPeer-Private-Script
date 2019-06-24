package org.maddev.ws.data;

import org.maddev.helpers.cron.BackgroundJob;
import org.maddev.ws.WebSocket;

import java.util.concurrent.TimeUnit;

public class UpdateConfig {

    public static void register() {
        BackgroundJob.enqueue(execute(), 30, TimeUnit.SECONDS);
    }

    private static Runnable execute() {
       return () -> WebSocket.getInstance().dispatch("get_config", new Object());
    }

}
