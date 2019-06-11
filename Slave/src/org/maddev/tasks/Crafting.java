package org.maddev.tasks;

import org.maddev.Config;
import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementUtil;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.maddev.helpers.time.TimeHelper;
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
import org.maddev.helpers.log.Logger;

import java.util.function.Predicate;

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
        return getLevel() < Config.CRAFTING_REQUIRED;
    }

    @Override
    public int execute() {
        Store.setAction("Crafting.");
        int level = getLevel();
        if (level < 10) {
            craftLeather();
        } else {
            spinFlax();
        }
        return Random.nextInt(350, 850);
    }

    private void craftLeather() {

        if (!Inventory.contains(s -> s.getName().equals("Leather") && !s.isNoted())) {
            if (!BankHelper.depositAllExcept(BankHelper.nearest(), s -> {
                String n = s.getName();
                if (s.isNoted()) return false;
                return n.equals("Needle") || n.equals("Thread") || n.equals("Leather");
            })) {
                return;
            }
        }

        if (!BankHelper.withdraw(
                new ItemPair("Needle", 1),
                new ItemPair("Thread", Integer.MAX_VALUE),
                new ItemPair("Leather", Integer.MAX_VALUE))) {
            return;
        }
        Bank.close();
        Store.setAction("Crafting leather.");
        InterfaceComponent component = getInterface();
        if (component != null) {
            component.click();
            TimeHelper.sleep(450, 850);
            TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), Random.nextInt(1750, 2500));
            return;
        }
        if (isAnimationDone(Random.nextInt(4000, 4500))) {
            Inventory.use(s -> s.getName().equals("Needle"), Inventory.getFirst("Leather"));
            TimeHelper.sleep(450, 850);
        }
    }

    private InterfaceComponent getInterface() {
        int level = getLevel();
        if (level < 7) {
            return MAKE_GLOVES_ADDRESS.resolve();
        }
        if (level < 10) {
            return MAKE_BOOTS_ADDRESS.resolve();
        }
        return MAKE_BOW_STRING_ADDRESS.resolve();
    }

    private void spinFlax() {
        Store.setTask("Spinning flax.");

        if(Players.getLocal().getPosition().getFloorLevel() == 0) {
            MovementUtil.applyLumbridgeFix();
            return;
        }

        Predicate<Item> flax = i -> i.getName().equals("Flax") && !i.isNoted();
        if (!Inventory.contains(flax) && !isAnimationDone(4000)) {
            //Ran out of flax, but we are still animating, just sleep a little bit so it doesnt look suspicious
            Store.setAction("Idling for a sec.");
            TimeHelper.sleep(500, 15000);
        }
        if(!Inventory.contains(flax)) {
            if(!BankHelper.depositAllExcept(BankLocation.LUMBRIDGE_CASTLE, flax)) {
                return;
            }
            if (!BankHelper.withdrawAll("Flax", BankLocation.LUMBRIDGE_CASTLE, true)) {
                return;
            }
            return;
        }
        if(!Inventory.contains(flax)) {
            Store.setAction("No flax.");
            return;
        }
        Store.setAction("Spinning flax.");
        switch (Players.getLocal().getFloorLevel()) {
            case 2: {
                InteractHelper.interact(SceneObjects.getFirstAt(new Position(3205, 3208, 2)));
                break;
            }
            case 1: {

                if (!isAnimationDone(Random.nextInt(4000, 4500))) {
                    Store.setAction("Spinning flax - Animating.");
                    return;
                }

                InterfaceComponent component = getInterface();

                if (component != null && component.isVisible()) {
                    component.click();
                    TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), Random.nextInt(1250, 2200));
                    return;
                }

                Position insideRoom = new Position(3208, 3214, 1);
                SceneObject door = SceneObjects.getFirstAt(new Position(3207, 3214, 1));
                if (door != null && insideRoom.distance() > door.distance()) {
                    Store.setAction("Opening door to wheel.");
                    InteractHelper.interact(door);
                    return;
                }

                SceneObject wheel = SceneObjects.getFirstAt(new Position(3209, 3212, 1));
                if (wheel == null) {
                    Store.setAction("Spinning wheel is null?");
                    return;
                }

                Store.setAction("Interacting with wheel.");
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
        if (Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
        }
        if (e.getSource() == Players.getLocal() && e.getCurrent() != -1) {
            lastAnim = System.currentTimeMillis();
        }
    }
}
