package org.maddev;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.log.Logger;
import org.maddev.paint.ScriptPaint;
import org.maddev.tasks.SubmitTasks;
import org.maddev.web.dax.DaxWeb;
import org.maddev.ws.WebSocket;
import org.maddev.ws.data.PlayerUpdateSender;
import org.maddev.ws.listeners.MessageListener;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.event.listeners.BankLoadListener;
import org.rspeer.runetek.event.listeners.ItemTableListener;
import org.rspeer.runetek.event.listeners.LoginResponseListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.BankLoadEvent;
import org.rspeer.runetek.event.types.ItemTableEvent;
import org.rspeer.runetek.event.types.LoginResponseEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.script.GameAccount;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskChangeListener;
import org.rspeer.script.task.TaskScript;

@ScriptMeta(developer = "MadDev", name = "Farm", desc = "The slave script", version = 1.3)
public class Main extends TaskScript implements RenderListener, BankLoadListener, ItemTableListener, TaskChangeListener, LoginResponseListener {

    private StopWatch runtime;
    private ScriptPaint paint;
    public static final Gson gson = new Gson();

    @Override
    public void onStart() {

        setupWebSocket();
        GameCanvas.setInputEnabled(true);

        //setAccount(new GameAccount("mental336@mailboxhub.de", "topnotch271"));
        DaxWeb.initialize("sub_FCt2YD8MiHPMRh", "8710d5e0-9ea4-4fef-aa9c-41c52f88b01b");

        runtime = StopWatch.start();
        paint = new ScriptPaint(this);
        submit(new SubmitTasks(this));
    }

    private void setupWebSocket() {
        try {
            WebSocket.getInstance().connect();
        } catch (Exception e) {
            Logger.severe("Failed to connect to WebSocket.");
            Logger.severe(e);
        }
        Logger.fine("Successfully connected to WebSocket.");
        PlayerUpdateSender.getInstance().start();
        MessageListener listener = new MessageListener();
        listener.initialize();
    }

    public StopWatch getRuntime() {
        return runtime;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            Logger.severe("Script has stopped.");
            PlayerUpdateSender.getInstance().dispose();
            WebSocket.getInstance().dispose();
            BankCache.dispose();
            Store.setState(org.maddev.State.SCRIPT_STOPPED);
        } catch (Exception e) {
            Logger.severe(e);
        }
    }

    @Override
    public void notify(RenderEvent e) {
        paint.notify(e);
    }

    @Override
    public void notify(BankLoadEvent bankLoadEvent) {
       BankCache.cache();
    }

    @Override
    public void notify(ItemTableEvent e) {
        if(Bank.isOpen()) {
            BankCache.cache();
        }
    }

    @Override
    public void notify(Task task, Task task1) {
        String to1 = task1 == null ? "Nothing" : task1.getClass().getSimpleName();
        Store.setTask(to1);
        Store.setAction(null);
    }

    @Override
    public void notify(LoginResponseEvent e) {
       String message = e.getResponse().getMessage();
       Logger.fine("login_response", e.getResponse().getMessage());
       if(message.contains("Your account has been disabled")) {
           JsonObject o = new JsonObject();
           o.addProperty("email", Game.getClient().getUsername());
           o.addProperty("date", System.currentTimeMillis());
           WebSocket.getInstance().dispatch("client_banned", o);
       }
    }
}
