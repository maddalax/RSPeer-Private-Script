package org.maddev.tasks.hunting;

import org.maddev.Config;
import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.awt.*;
import java.util.HashMap;

public class Hunting extends Task implements RenderListener, ChatMessageListener {

    public static final Position MIDDLE = new Position(2351, 3589);
    private Position trap;
    private HashMap<Position, Long> created;
    private long clearTime = Random.nextInt(120000, 210000);

    public Hunting() {
        created = new HashMap<>();
        Game.getEventDispatcher().register(this);
    }

    @Override
    public boolean validate() {
        int level = Skills.getCurrentLevel(Skill.HUNTER);
        return level >= 9 && level < Config.HUNTING_REQUIRED;
    }

    @Override
    public int execute() {
        doExecute();
        return Random.nextInt(350, 650);
    }

    private void doExecute() {
        Log.fine("Hunting.");
        if (MIDDLE.distance() > 600 || !Inventory.contains(s -> s.getName().equals("Bird snare") && !s.isNoted())) {
            Log.fine("Getting supplies.");
            getSupplies();
            return;
        }

        Store.setStatus("Hunting");
        if (!MIDDLE.isLoaded()) {
            Log.fine("Walking to middle.");
            MovementHelper.walkRandomized(MIDDLE, false);
            return;
        }

        dropGarbage();

        if (!Players.getLocal().isAnimating()) {
            Pickable snare = Pickables.getNearest("Bird snare");
            if (snare != null && snare.containsAction("Take")) {
                if (snare.getPosition().equals(trap)) {
                    Log.fine("Removing trap!");
                    created.remove(trap);
                    trap = null;
                }
                InteractHelper.interact(snare, "Take");
                Time.sleep(850, 2250);
                return;
            }
        }

        SceneObject active = getActiveSnare();
        if (active != null) {
            if (trap != null) {
                if (!created.containsKey(trap)) {
                    created.put(trap, System.currentTimeMillis());
                }
                long start = created.get(trap);
                if (System.currentTimeMillis() - start > clearTime) {
                    Log.fine("Been " + clearTime + " ms. Picking up trap.");
                    if (active.interact("Dismantle")) {
                        if (getActiveSnare() == null) {
                            Log.fine("Snare is null. Clearing");
                            created.remove(trap);
                            trap = null;
                            clearTime = Random.nextInt(120000, 210000);
                        }
                    }
                    return;
                }
            }
            return;
        }

        SceneObject broken = getBrokenSnare();
        if (broken != null) {
            Log.fine("Picking up trap.");
            created.remove(trap);
            trap = null;
            InteractHelper.interact(broken, "Dismantle");
            Time.sleep(350, 650);
            return;
        }
        SceneObject caught = getCaught();
        if (caught != null) {
            Log.fine("Picking up caught trap.");
            created.remove(trap);
            trap = null;
            InteractHelper.interact(caught, "Check");
            Time.sleep(350, 650);
            return;
        }
        if (!Players.getLocal().isAnimating()) {
            placeTrap();
        }
    }

    private boolean getSupplies() {
        String games = PlayerHelper.getFirst(EquipmentHelper.getGamesNecklaces());
        if (games == null) {
            Store.setStatus("No games necklace found.");
            return false;
        }
        if (!BankHelper.withdrawOnly(BankLocation.getNearest(), true,
                new ItemPair(games, 1),
                new ItemPair("Bird snare", Integer.MAX_VALUE))) {
            return false;
        }

        if (!Players.getLocal().isAnimating()) {
            EquipmentHelper.teleportNecklace(false, "Barbarian Outpost");
            Time.sleep(850, 1500);
        }

        return false;
    }

    private void placeTrap() {
        if (trap == null) {
            Position p = MIDDLE.randomize(3);
            if (!Movement.isWalkable(p, false)) {
                return;
            }
            trap = p;
        }
        if (!Players.getLocal().getPosition().equals(trap)) {
            Log.fine("Walking to trap." + " " + trap.toString());
            if (!Movement.setWalkFlagWithConfirm(trap)) {
                Log.fine("Could not walk to trap, resetting.");
                trap = null;
            }
            Time.sleep(800, 1800);
            return;
        }
        Item snare = Inventory.getFirst("Bird snare");
        if (snare == null) {
            Log.fine("No bird snare?");
            return;
        }
        InteractHelper.interact(snare, "Lay");
        Time.sleep(1200, 2200);
        Time.sleepUntil(() -> Players.getLocal().isAnimating(), 2000);
    }

    private SceneObject getActiveSnare() {
        if (trap == null) return null;
        var snare = SceneObjects.getFirstAt(trap);
        if (snare == null || !snare.containsAction("Investigate")) {
            return null;
        }
        return snare;
    }

    private void dropGarbage() {
        for (Item item : Inventory.getItems(s -> s.getName().
                equals("Raw bird meat") || s.getName().equals("Bones"))) {
            item.interact("Drop");
            Time.sleep(230, 450);
        }
    }

    private SceneObject getBrokenSnare() {
        if (trap == null) return null;
        var snare = SceneObjects.getFirstAt(trap);
        if (snare == null || snare.containsAction("Investigate")) {
            return null;
        }
        return snare.containsAction("Dismantle") ? snare : null;
    }

    private SceneObject getCaught() {
        if (trap == null) return null;
        var snare = SceneObjects.getFirstAt(trap);
        if (snare == null || snare.containsAction("Investigate")) {
            return null;
        }
        return snare.containsAction("Check") ? snare : null;
    }


    @Override
    public void notify(RenderEvent e) {
        if (trap != null) {
            e.getSource().setColor(Color.PINK);
            trap.outline(e.getSource());
        }
        if (Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
        }
    }

    @Override
    public void notify(ChatMessageEvent e) {
        if (e.getType() == ChatMessageType.PUBLIC) {
            return;
        }
        if (e.getMessage().contains("You can't lay a trap")) {
            Log.fine("Can't lay a trap here.");
            if (trap != null) {
                created.remove(trap);
                trap = null;
            }
        }
        if (e.getMessage().contains("You begin setting up the trap")) {
            trap = Players.getLocal().getPosition();
            Log.fine("New trap position: " + trap);
        }
        if (e.getMessage().contains("You may set up only one trap")) {
            Log.fine("Somehow we lost our trap position?");
            new Thread(() -> {
                SceneObject snare = SceneObjects.getNearest("Bird snare");
                if (snare != null && snare.containsAction("Dismantle")) {
                    InteractHelper.interact(snare, "Dismantle");
                    Time.sleep(800, 1600);
                }
            }).start();
        }
    }
}
