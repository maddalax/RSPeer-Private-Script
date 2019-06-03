package org.maddev;

import org.maddev.helpers.bank.BankCache;
import org.maddev.paint.ScriptPaint;
import org.maddev.tasks.SubmitTasks;
import org.maddev.web.dax.DaxWeb;
import org.rspeer.runetek.api.commons.StopWatch;
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
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskChangeListener;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "MadDev", name = "Farm", desc = "The slave script")
public class Main extends TaskScript implements RenderListener, BankLoadListener, ItemTableListener, TaskChangeListener {

    private StopWatch runtime;
    private ScriptPaint paint;

    @Override
    public void onStart() {

        GameCanvas.setInputEnabled(true);
        setAccount(new GameAccount("Chubbyownzaa+10@gmail.com", "maddev11"));

        DaxWeb.initialize("sub_DPjcfqN4YkIxm8", "PUBLIC-KEY");

        runtime = StopWatch.start();
        paint = new ScriptPaint(this);
        submit(new SubmitTasks(this));
    }

    public StopWatch getRuntime() {
        return runtime;
    }

    @Override
    public void onStop() {
        Log.fine("Script has stopped.");
        Store.setState(org.maddev.State.SCRIPT_STOPPED);
        super.onStop();
    }

    @Override
    public void notify(RenderEvent e) {
        paint.notify(e);
    }

    @Override
    public void notify(BankLoadEvent bankLoadEvent) {
       Log.fine("Bank has loaded.");
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
        String to = task == null ? "Nothing" : task.getClass().getSimpleName();
        String to1 = task1 == null ? "Nothing" : task1.getClass().getSimpleName();
        Log.fine("Task has changed from: " + to + " to " + to1);
        Store.setTask(to);
        Store.setAction(null);
    }
}
