package org.maddev.tasks;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.dialogue.DialogueHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LostCity extends Task implements RenderListener {

    public static boolean isComplete() {
        return Varps.get(147) == 6;
    }

    private Position farthestSpot = new Position(2848, 9754, 0);
    private Position zombieSafeSpot = new Position(2848, 9745, 0);
    private Position treeSafeSpot = new Position(2859, 9731, 0);
    private Position treeSpot = new Position(2860, 9733, 0);

    @Override
    public boolean validate() {
        return !isComplete();
    }

    @Override
    public int execute() {
        doExecute();
        return Random.nextInt(450, 850);
    }

    private void doExecute() {

        Store.setStatus("Lost City");

        int varp = Varps.get(147);

        if (!withdrawItems(varp)) {
            return;
        }

        if(Health.getPercent() <= 50) {
            Item lobster = Inventory.getFirst("Lobster");
            if(lobster == null) {
                Log.fine("Yikes no lobster, we are dead boys.");
            }
            else {
                lobster.click();
                Time.sleep(100, 250);
            }
        }

        Item glory = EquipmentHelper.getChargedGlory();
        if(glory != null && !Equipment.contains(glory.getName())) {
            glory.click();
            Time.sleep(450, 850);
        }

        if (varp == 0) {
            startQuest();
            return;
        }
        if (varp == 1) {
            interactTree();
            return;
        }
        if (varp == 2) {
            if (Players.getLocal().getPosition().getY() < 9000) {
                goToEntrana();
            } else {
                handleCave();
            }
            return;
        }

        if(varp > 2) {
            finishQuest();
        }
    }

    private void finishQuest() {
        // Teleport out of cave.
        if(Players.getLocal().getPosition().getY() > 9000) {

            Item branch = Inventory.getFirst("Dramen branch");
            if(branch != null) {
                Inventory.use(s -> s.getName().equals("Knife"), branch);
                Time.sleep(450, 850);
                return;
            }
            int count = Inventory.getCount("Dramen staff");
            if(count < 5) {
                int diff = 5 - count;
                if(Inventory.getFreeSlots() < diff) {
                    Item lobster = Inventory.getFirst("Lobster");
                    lobster.interact("Drop");
                    Time.sleep(250, 450);
                    return;
                }
                SceneObject tree = SceneObjects.getNearest("Dramen tree");
                if(tree == null) {
                    Store.setStatus("Failed to find dramen tree after killing spirit.");
                    return;
                }
                InteractHelper.interact(tree, "Chop down");
                Time.sleepUntil(() -> Players.getLocal().isAnimating(), 1500);
                return;
            }

            Item glory = EquipmentHelper.getChargedGlory();
            if(glory != null) {
                if(Equipment.contains(s -> s.getName().equals(glory.getName()))) {
                    EquipmentSlot.NECK.interact("Draynor Village");
                    Time.sleep(850, 1880);
                }
                return;
            }
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            Time.sleep(5500, 6500);
            return;
        }

        if(!Equipment.contains("Dramen staff") && Inventory.contains("Dramen staff")) {
            Inventory.getFirst("Dramen staff").click();
            Time.sleep(230, 330);
        }

        Position finish = new Position(3200, 3169, 0);
        if(!finish.isLoaded() || finish.distance() > 10) {
            MovementHelper.walkRandomized(finish, false);
            Time.sleep(230, 450);
            return;
        }
        SceneObject door = SceneObjects.getNearest("Door");
        if(door == null) {
            Store.setStatus("Failed to find door to Zanaris.");
            return;
        }
        InteractHelper.interact(door, "Open");
        Time.sleep(350, 850);
    }

    private void goToEntrana() {
        if (!Area.rectangular(2790, 3398, 2880, 3323).setIgnoreFloorLevel(true).contains(Players.getLocal())) {
            Store.setStatus("Going to Entrana.");
            Position monkTile = new Position(3042, 3241, 0);
            if (!monkTile.isLoaded() || monkTile.distance() > 10) {
                MovementHelper.walkRandomized(monkTile, false);
                Time.sleep(560, 850);
                return;
            }
            Npc monk = Npcs.getNearest("Monk of Entrana");
            if (monk == null) {
                Store.setStatus("Failed to find Monk to Entrana.");
                return;
            }
            InteractHelper.interact(monk, "Take-boat");
            Time.sleep(850, 1120);
            return;
        }

        if(Players.getLocal().getPosition().getFloorLevel() == 1) {
            SceneObject plank = SceneObjects.getFirstAt(new Position(2834, 3333, 1));
            if(plank != null) {
                Log.fine("Crossing plank.");
                plank.click();
                Time.sleep(850, 1120);
                return;
            }
        }
        //2848, 9754

        //Near ladder
        if (!Dialog.isOpen()) {
            Position ladderPostion = new Position(2821, 3374, 0);
            if (ladderPostion.distance() > 5) {
                MovementHelper.walkRandomized(ladderPostion, false);
                return;
            }
            SceneObject ladder = SceneObjects.getNearest("Ladder");
            InteractHelper.interact(ladder, "Climb-down");
            Time.sleep(850, 1500);
        }
        DialogueHelper.process(null, "Well that is a risk");
    }

    private void handleCave() {
        Store.setStatus("Handling cave.");
        // Get axe from zombie.
        if (!Inventory.contains(s -> s.getName().contains("axe"))) {

            Pickable axe = Pickables.getNearest(x -> x.getName().contains("axe"));

            if (axe != null && axe.isPositionInteractable()) {

                if(Inventory.isFull()) {
                    Item lobster = Inventory.getFirst("Lobster");
                    if(lobster == null) {
                        Store.setStatus("Inventory is full, but no lobsters?");
                        return;
                    }
                    lobster.interact("Drop");
                    Time.sleep(350, 550);
                }
                Log.fine("Trying to pickup axe.");
                axe.interact("Take");
                Time.sleep(850, 1950);
                return;
            }

            boolean inSafeZone = Players.getLocal().getPosition().equals(zombieSafeSpot);
            if (!inSafeZone) {
                MovementHelper.setWalkFlag(zombieSafeSpot);
                return;
            }
            Npc zombie = Npcs.getNearest(s ->
                    s.getName().equals("Zombie") &&
                            (s.getTarget() == null || s.getTarget().equals(Players.getLocal())) && s.distance() <= farthestSpot.distance());
            if (zombie == null) {
                Store.setStatus("Waiting for zombie.");
                return;
            }
            Spell spell = Skills.getCurrentLevel(Skill.MAGIC) >= 13 ? Spell.Modern.FIRE_STRIKE : Spell.Modern.WIND_STRIKE;
            Magic.cast(spell, zombie);
            Time.sleep(300, 450);
            return;
        }

        final Npc spirit = Npcs.getNearest("Tree spirit");

        if(spirit == null) {
            if(!Players.getLocal().getPosition().equals(treeSpot)) {
                MovementHelper.setWalkFlag(treeSpot);
                return;
            }
            SceneObject tree = SceneObjects.getNearest("Dramen tree");
            if(tree == null) {
                Store.setStatus("Failed to find dramen tree and spirit.");
                return;
            }
            if(!tree.interact("Chop down")) {
                Time.sleep(100, 250);
            }
            Time.sleep(50, 100);
            return;
        }

        if(!Players.getLocal().getPosition().equals(treeSafeSpot)) {
            Movement.setWalkFlag(treeSafeSpot);
            Time.sleep(50, 100);
            return;
        }

        Magic.cast(Spell.Modern.FIRE_STRIKE, spirit);
        Time.sleep(100, 250);

    }

    private void startQuest() {
        Store.setStatus("Starting Lost City.");
        Position startTile = new Position(3148, 3205, 0);
        if (!startTile.isLoaded() || startTile.distance() > 10) {
            MovementHelper.walkRandomized(startTile, false);
            return;
        }
        DialogueHelper.process("Warrior", "What are you camped", "Who's Zanaris", "If it's hidden", "Looks like you don't");
    }

    private void interactTree() {
        Npc shamus = Npcs.getNearest("Shamus");
        if (shamus == null) {
            SceneObject tree = SceneObjects.getNearest(s -> s.getName().equals("Tree") && s.containsAction("Chop"));
            if (tree == null) {
                MovementHelper.walkRandomized(new Position(3139, 3211, 0), false);
                Time.sleep(230, 545);
                return;
            }
            InteractHelper.interact(tree, "Chop");
            Time.sleep(1200, 1800);
            return;
        }
        DialogueHelper.processWithEntity(shamus, "I've been in that shed");
    }

    private boolean withdrawItems(int varp) {

        if (varp < 2 || Players.getLocal().getPosition().getY() > 9000) {
            return true;
        }

        boolean goingToEntrana = varp == 2;

        if (goingToEntrana) {
            if(!BankHelper.depositAllExcept(BankLocation.DRAYNOR, s -> s.getName().contains("rune")
                    || s.getName().equals("Knife")
                    || s.getName().contains("Amulet of glory")
                    || s.getName().equals("Lobster") && !s.isNoted())) {
                return false;
            }
        }

        if (!goingToEntrana) {
            BankHelper.depositAllExcept(BankLocation.getNearest(), s -> s.getName().contains("rune")
                    || s.getName().equals("Knife")
                    || s.getName().equals("Adamant axe")
                    || s.getName().contains("Amulet of glory")
                    || s.getName().equals("Lobster"));
        }

        List<ItemPair> toWithdraw = new ArrayList<>();

        // Going to entrana
        if (varp == 2) {
            toWithdraw.add(new ItemPair("Air rune", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Mind rune", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Water rune", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Earth rune", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Fire rune", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Lobster", Integer.MAX_VALUE));
            toWithdraw.add(new ItemPair("Knife", 1));
        }

        if (varp > 2) {
            toWithdraw.add(new ItemPair("Adamant axe", 1));
        }


        for (ItemPair pair : toWithdraw) {
            if (!BankHelper.withdraw(pair)) {
                return false;
            }
        }

        Item glory = EquipmentHelper.getChargedGlory();
        if (glory == null) {
            List<String> glories = Arrays.asList(EquipmentHelper.getChargedGlories());
            return BankHelper.withdraw(item -> glories.contains(item.getName()), 1);
        }

        return true;
    }

    @Override
    public void notify(RenderEvent e) {
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
            return;
        }
        e.getSource().setColor(Color.PINK);
        if(zombieSafeSpot.isLoaded()) {
            zombieSafeSpot.outline(e.getSource());
        }
        if(farthestSpot.isLoaded()) {
            farthestSpot.outline(e.getSource());
        }
        if(treeSafeSpot.isLoaded()) {
            treeSafeSpot.outline(e.getSource());
        }
        if(treeSpot.isLoaded()) {
            treeSpot.outline(e.getSource());
        }
    }
}
