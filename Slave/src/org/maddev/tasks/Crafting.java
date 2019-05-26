package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.InterfaceAddress;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.AnimationListener;
import org.rspeer.runetek.event.types.AnimationEvent;
import org.rspeer.script.task.Task;

public class Crafting extends Task implements AnimationListener {

    private long lastAnim;

    public Crafting() {
        Game.getEventDispatcher().register(this);
    }

    private int getLevel() {
        return Skills.getCurrentLevel(Skill.CRAFTING);
    }

    private static final InterfaceAddress MAKE_GLOVES_ADDRESS = new InterfaceAddress(
            () -> Interfaces.getFirst(270, x -> x.getName().toLowerCase().contains("leather gloves"))
    );

    private static final InterfaceAddress MAKE_BOOTS_ADDRESS = new InterfaceAddress(
            () -> Interfaces.getFirst(270, x -> x.getName().toLowerCase().contains("leather boots"))
    );

    private static final InterfaceAddress MAKE_BOW_STRING_ADDRESS = new InterfaceAddress(
            () -> Interfaces.getFirst(270, x -> x.getName().toLowerCase().contains("bow string"))
    );

    @Override
    public boolean validate() {
        return getLevel() < 31;
    }

    @Override
    public int execute() {
        int level = getLevel();
        if(level < 10) {
            craftLeather();
        }
        else {
            spinFlax();
        }
        return Random.nextInt(350, 850);
    }

    private void craftLeather() {
        if(!BankHelper.withdraw(
                new ItemPair("Needle", 1),
                new ItemPair("Thread", Integer.MAX_VALUE),
                new ItemPair("Leather", Integer.MAX_VALUE))) {
            return;
        }
        Bank.close();
        Store.setStatus("Crafting leather.");
        InterfaceComponent component = getInterface();
        if(component != null) {
            component.click();
            Time.sleep(450, 850);
            Time.sleepUntil(() -> Players.getLocal().isAnimating(), Random.nextInt(1750, 2500));
            return;
        }
        if (isAnimationDone(Random.nextInt(4000, 4500))) {
            Inventory.use(s -> s.getName().equals("Needle"), Inventory.getFirst("Leather"));
            Time.sleep(450, 850);
        }
    }

    private InterfaceComponent getInterface() {
       int level = getLevel();
       if(level < 7) {
           return MAKE_GLOVES_ADDRESS.resolve();
       }
       if(level < 10) {
           return MAKE_BOOTS_ADDRESS.resolve();
       }
       return MAKE_BOW_STRING_ADDRESS.resolve();
    }

    private void spinFlax() {
        if(!Inventory.contains("Flax") && !isAnimationDone(4000)) {
            //Ran out of flax, but we are still animating, just sleep a little bit so it doesnt look suspicious
            Store.setStatus("Idling for a sec.");
            Time.sleep(500, 15000);
        }
       if(!BankHelper.withdrawAll("Flax", BankLocation.LUMBRIDGE_CASTLE, true)) {
           return;
       }
       Store.setStatus("Spinning flax.");
       switch (Players.getLocal().getFloorLevel()) {
           case 2: {
               InteractHelper.interact(SceneObjects.getFirstAt(new Position(3205, 3208, 2)));
               break;
           }
           case 1: {

               if (!isAnimationDone(Random.nextInt(4000, 4500))) {
                   Store.setStatus("Spinning flax - Animating.");
                   return;
               }

               InterfaceComponent component = getInterface();

               if(component != null && component.isVisible()) {
                   component.click();
                   Time.sleepUntil(() -> Players.getLocal().isAnimating(), Random.nextInt(1250, 2200));
                   return;
               }

               Position insideRoom = new Position(3208, 3214, 1);
               SceneObject door = SceneObjects.getFirstAt(new Position(3207, 3214, 1));
               if(door != null && insideRoom.distance() > door.distance()) {
                   Store.setStatus("Opening door to wheel.");
                   InteractHelper.interact(door);
                   return;
               }

               SceneObject wheel = SceneObjects.getFirstAt(new Position(3209, 3212, 1));
               if(wheel == null) {
                   Store.setStatus("Spinning wheel is null?");
                   return;
               }

               Store.setStatus("Interacting with wheel.");
               InteractHelper.interact(wheel, "Spin");
               return;
           }
           case 0: {
               BankHelper.open(BankLocation.LUMBRIDGE_CASTLE, true);
           }
       }
    }

    private boolean isAnimationDone(int timeout) {
        return System.currentTimeMillis() > (lastAnim + timeout);
    }

    @Override
    public void notify(AnimationEvent e) {
       if(e.getSource() == Players.getLocal() && e.getCurrent() != -1) {
           lastAnim = System.currentTimeMillis();
       }
    }
}
