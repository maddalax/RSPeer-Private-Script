package org.maddev;

import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.walking.CustomPath;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.tasks.SubmitTasks;
import org.maddev.web.dax.DaxWeb;
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
        GameCanvas.setInputEnabled(true);
        setAccount(new GameAccount("Chubbyownzaa+10@gmail.com", "maddev11"));

        DaxWeb.initialize("sub_DPjcfqN4YkIxm8", "PUBLIC-KEY");
        //submit(new WalkTest());

        submit(new SubmitTasks(this));
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
