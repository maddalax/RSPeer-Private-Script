package org.maddev;

import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.walking.CustomPath;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.walking.paths.HuntingHillGiants;
import org.maddev.helpers.walking.paths.LumbridgeHut;
import org.maddev.tasks.SubmitTasks;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.event.listeners.BankLoadListener;
import org.rspeer.runetek.event.listeners.ItemTableListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.BankLoadEvent;
import org.rspeer.runetek.event.types.ItemTableEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.script.GameAccount;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;

import java.awt.*;

@ScriptMeta(developer = "MadDev", name = "Chubby Farm Slave Script", desc = "The slave script")
public class Main extends TaskScript implements RenderListener, BankLoadListener, ItemTableListener {

    @Override
    public void onStart() {
        setAccount(new GameAccount("Chubbyownzaa+11@gmail.com", "maddev11"));
        Game.getClient().setWorld(Worlds.get(s -> s.isMembers() && !s.isSkillTotal()
                && !s.isTournament()));
        GameCanvas.setInputEnabled(true);

        submit(new SubmitTasks(this));

        MovementHelper.addCustomPath(new LumbridgeHut());
        MovementHelper.addCustomPath(new HuntingHillGiants());
    }

    @Override
    public void onStop() {
        Log.fine("Stopped");
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
        if(Store.getStatus() != null) {
            g.drawString("Status: " + Store.getStatus(), 320, 45);
        }
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
}
