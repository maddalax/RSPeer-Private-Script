package org.maddev.ws.data;

import org.maddev.ws.WebSocket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerUpdateSender {

    private static PlayerUpdateSender instance;

    public static PlayerUpdateSender getInstance() {
        if(instance == null) {
            instance = new PlayerUpdateSender();
        }
        return instance;
    }

    private ScheduledExecutorService service;

    private PlayerUpdateSender() {
        service = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        service.scheduleAtFixedRate(this::sendPlayerData, 1, 5, TimeUnit.SECONDS);
     }

    private void sendPlayerData() {
        WebSocket.getInstance().dispatch("client_info", ClientInfoAggregator.execute());
    }

    public void dispose() {
        if(service != null) {
            service.shutdown();
        }
        service = null;
    }

}
