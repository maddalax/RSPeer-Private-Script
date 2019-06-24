package org.maddev.ws.data;

import org.maddev.helpers.cron.BackgroundJob;
import org.maddev.ws.WebSocket;

import java.util.concurrent.TimeUnit;

public class PlayerUpdateSender {

    public static void start() {
        BackgroundJob.enqueue(PlayerUpdateSender::sendPlayerData, 30, TimeUnit.SECONDS);
     }

    private static void sendPlayerData() {
        WebSocket.getInstance().dispatch("client_info", ClientInfoAggregator.execute());
    }
}
