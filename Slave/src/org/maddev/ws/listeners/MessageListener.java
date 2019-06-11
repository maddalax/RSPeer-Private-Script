package org.maddev.ws.listeners;

import com.google.gson.JsonObject;
import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.log.Logger;
import org.maddev.ws.WebSocket;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.maddev.helpers.log.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class MessageListener implements RenderListener  {

    private boolean takingScreenshot;

    public MessageListener() {
        Game.getEventDispatcher().register(this);
    }

    public void initialize() {
        onExit();
    }

    private void onExit() {
        WebSocket.getInstance().registerListener("client_exit", (message) -> {
            Logger.severe("Closing client due to receiving exit command from server.");
            System.exit(0);
        });
        WebSocket.getInstance().registerListener("client_screenshot", (any) -> {
            Logger.fine("Taking screenshot.");
            takingScreenshot = true;
        });
    }

    @Override
    public void notify(RenderEvent e) {
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
            return;
        }
        if(!takingScreenshot) {
            return;
        }
        new Thread(() -> {
            takingScreenshot = false;
            BufferedImage image = (BufferedImage) e.getProvider().getImage();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "jpg", bos );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            byte [] data = bos.toByteArray();
            Logger.fine("Size: " + data.length);
            JsonObject o = new JsonObject();
            o.addProperty("rsn", Players.getLocal().getName());
            o.addProperty("data", Base64.getEncoder().encodeToString(data));
            WebSocket.getInstance().dispatch("client_screenshot", o);
        }).start();
        takingScreenshot = false;
    }
}
