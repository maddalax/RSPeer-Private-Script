package org.maddev.tasks.zanaris;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.maddev.tasks.LostCity;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.InterfaceAddress;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;

public class Zanaris extends Task {

    @Override
    public boolean validate() {
        return LostCity.isComplete();
    }

    private InterfaceAddress EXCHANGE = new InterfaceAddress(() -> Interfaces.getFirst(540, s -> s.getText().contains("Elnock's Exchange")));

    private InterfaceAddress JAR_GENERATOR = new InterfaceAddress(() -> Interfaces.getFirst(540, s -> s.getText().contains("Jar generator")));


    @Override
    public int execute() {
        int loop = Random.nextInt(350, 550);
        if(ZanarisHelper.inPuroPuro()) {
            handlePuroPuro();
            return loop;
        }
        if(Inventory.contains("Impling jar")) {
            handleImplingJar();
            return loop;
        }
        if(!ZanarisHelper.inZanaris()) {
            Store.setTask("Going to Zanaris.");
            ZanarisHelper.goToZanaris(true);
            return loop;
        }
        return loop;
    }

    private void handlePuroPuro() {
        InterfaceComponent exchange = EXCHANGE.resolve();
        if(exchange != null && exchange.isVisible()) {
            InterfaceComponent generator = Interfaces.getComponent(540, 120);
            if(generator == null) {
                Store.setAction("Failed to find generator interface.");
                return;
            }
            Store.setAction("Clicking Jar Generator.");
            generator.click();
            Time.sleep(1500, 3500);
            InterfaceComponent ok = Interfaces.getComponent(540, 125);
            if(ok == null) {
                Store.setAction("Failed to find ok interface.");
                return;
            }
            Store.setAction("Clicking Confirm.");
            ok.interact("Ok");
            Time.sleep(1500, 2000);
            return;
        }
        if(!Inventory.contains("Impling jar")) {
            Npc elnok = Npcs.getNearest("Elnock Inquisitor");
            if (elnok == null) {
                Store.setAction("Failed to find elnok.");
                return;
            }
            InteractHelper.interact(elnok, "Trade");
            Time.sleep(850, 1220);
            return;
        }
        if(!Players.getLocal().isAnimating()) {
            SceneObject portal = SceneObjects.getNearest("Portal");
            if(portal == null) {
                Store.setAction("Failed to find portal.");
                return;
            }
            InteractHelper.interact(portal, "Escape");
            Time.sleep(850, 1220);
        }
    }

    private void handleImplingJar() {
        if(Bank.depositAllExcept("Impling jar")) {

        }
    }
}
