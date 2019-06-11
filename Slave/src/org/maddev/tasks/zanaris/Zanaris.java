package org.maddev.tasks.zanaris;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.time.TimeHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.maddev.tasks.LostCity;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.InterfaceAddress;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.task.Task;

public class Zanaris extends Task implements ChatMessageListener {

    @Override
    public boolean validate() {
        return LostCity.isComplete();
    }

    private InterfaceAddress EXCHANGE = new InterfaceAddress(() -> Interfaces.getFirst(540, s -> s.getText().contains("Elnock's Exchange")));

    private InterfaceAddress JAR_GENERATOR = new InterfaceAddress(() -> Interfaces.getFirst(540, s -> s.getText().contains("Jar generator")));

    private static int chargesLeft = -1;

    public static int getChargesLeft() {
        return chargesLeft;
    }

    public Zanaris() {
        Game.getEventDispatcher().register(this);
    }

    @Override
    public int execute() {
        int loop = Random.nextInt(350, 550);
        if(ZanarisHelper.inPuroPuro()) {
            handlePuroPuro();
            return loop;
        }
        if(Inventory.getItems(Item::isNoted).length > 0) {
            BankHelper.depositAllExcept(BankHelper.nearest(), s -> false);
            return loop;
        }
        if(PlayerHelper.hasAny("Jar generator")) {
            handleImplingJar();
            return loop;
        }
        if(!ZanarisHelper.inZanaris()) {
            Store.setTask("Going to Zanaris.");
            ZanarisHelper.goToZanaris(true);
            return loop;
        }
        getImplingJar();
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
            TimeHelper.sleep(1500, 3500);
            InterfaceComponent ok = Interfaces.getComponent(540, 126);
            if(ok == null) {
                Store.setAction("Failed to find ok interface.");
                return;
            }
            Store.setAction("Clicking Confirm.");
            ok.click();
            TimeHelper.sleep(1500, 4500);
            return;
        }
        if(!Inventory.contains("Jar generator")) {
            Npc elnok = Npcs.getNearest("Elnock Inquisitor");
            if (elnok == null) {
                Store.setAction("Failed to find elnok.");
                return;
            }
            InteractHelper.interact(elnok, "Trade");
            TimeHelper.sleep(850, 1220);
            return;
        }
        Store.setAction("Exiting portal.");
        if(!Players.getLocal().isAnimating()) {
            SceneObject portal = SceneObjects.getNearest("Portal");
            if(portal == null) {
                Store.setAction("Failed to find portal.");
                return;
            }
            InteractHelper.interact(portal, "Escape");
            TimeHelper.sleep(850, 1220);
        }
    }

    private void handleImplingJar() {
        if(ZanarisHelper.BANK.distance() > 5) {
            MovementHelper.setWalkFlag(ZanarisHelper.BANK);
            TimeHelper.sleep(850, 1000);
            return;
        }
        if(Inventory.isFull()) {
            if(!BankHelper.depositAllExcept(BankHelper.nearest(), s -> s.getName().equals("Jar generator"))) {
                return;
            }
        }
        if(!Inventory.contains("Jar generator")) {
            BankHelper.withdraw("Jar generator", 1);
            return;
        }
        Store.setAction("Using Jar generator - " + chargesLeft);
        String action = chargesLeft == 1 ? "Butterfly-jar" : "Impling-jar";
        Item i = Inventory.getFirst("Jar generator");
        if(i == null) {
            Store.setAction("Failed to get jar generator?");
            return;
        }
        i.interact(action);
        Time.sleep(300, 450);
    }

    private void getImplingJar() {
        ItemPair[] items = new ItemPair[] {
                new ItemPair("Essence impling jar", 3),
                new ItemPair("Eclectic impling jar", 2),
                new ItemPair("Nature impling jar", 1)
        };
        if(!BankHelper.withdrawOnly(BankHelper.nearest(), false, items)) {
            return;
        }
        SceneObject circle = SceneObjects.getNearest("Centre of crop circle");
        if(circle == null || circle.distance() > 15) {
            Store.setAction("Walking to circle.");
            MovementHelper.setWalkFlag(ZanarisHelper.PURO_PURO_PEN);
            return;
        }
        Store.setAction("Interacting with circle.");
        if(InteractHelper.interact(circle, "Enter")) {
            TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), 2500);
        }
    }

    @Override
    public void notify(ChatMessageEvent e) {
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
        }
        if(e.getType() == ChatMessageType.PUBLIC || !e.getMessage().contains("charges left in your jar generator")) {
            return;
        }
        chargesLeft = Integer.parseInt(e.getMessage().replace("You have ", "").replace("charges left in your jar generator.", "").trim());
    }
}
