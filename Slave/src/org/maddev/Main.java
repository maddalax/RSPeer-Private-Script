package org.maddev;

import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.walking.CustomPath;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.walking.paths.LumbridgeHut;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.event.listeners.BankLoadListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.BankLoadEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.script.GameAccount;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.maddev.tasks.GrandExchange;

import java.awt.*;

@ScriptMeta(developer = "MadDev", name = "Chubby Farm Slave Script", desc = "The slave script")
public class Main extends TaskScript implements RenderListener, BankLoadListener {

    @Override
    public void onStart() {
        setAccount(new GameAccount("rockstar_sugarfree2@maddev.me", "hykjmjrj11"));
        Game.getClient().setWorld(Worlds.get(s -> s.isMembers() && !s.isSkillTotal()
                && !s.isTournament()));
        GameCanvas.setInputEnabled(true);
        submit(new GrandExchange());
        MovementHelper.addCustomPath(new LumbridgeHut());
    }

    @Override
    public void onStop() {
        System.out.println("Stopped");
        Store.setState(org.maddev.State.SCRIPT_STOPPED);
        super.onStop();
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics2D g = (Graphics2D) e.getSource();
        CustomPath path = MovementHelper.getCustomPath();
        if(path != null) {
            g.drawString("Custom Path: " + path.getName(), 320, 20);
        }
    }

    @Override
    public void notify(BankLoadEvent bankLoadEvent) {
        new Thread(() -> {
            System.out.println("Loaded bank.");
            Time.sleep(1000);
            System.out.println("Cached.");
            BankCache.cache();
        }).start();
    }
}
