package org.maddev.tasks;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.dialogue.DialogueHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.time.TimeHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.BankLocation;
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
import org.rspeer.script.task.TaskChangeListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class LostCity extends Task implements RenderListener, TaskChangeListener {

    public static boolean isComplete() {
        return Varps.get(147) == 6;
    }

    private static boolean valid = false;

    public static boolean mayNeedSupplies() {
        if(Players.getLocal().getPosition().getY() > 9000 && Inventory.contains("Mind rune")) {
            return false;
        }
        return Varps.get(147) < 5;
    }

    private Position farthestSpot = new Position(2848, 9754, 0);
    private Position zombieSafeSpot = new Position(2848, 9745, 0);
    private Position treeSafeSpot = new Position(2859, 9731, 0);
    private Position treeSpot = new Position(2860, 9733, 0);

    @Override
    public boolean validate() {
        valid = !isComplete();
        return valid;
    }

    @Override
    public int execute() {
        doExecute();
        return Random.nextInt(450, 850);
    }

    private void doExecute() {

        Store.setAction("Lost City");

        int varp = Varps.get(147);

        if (!withdrawItems(varp)) {
            return;
        }

        if(Health.getPercent() <= 50) {
            Item lobster = Inventory.getFirst("Lobster");
            if(lobster == null) {
                Logger.fine("Yikes no lobster, we are dead boys.");
            }
            else {
                lobster.click();
                TimeHelper.sleep(100, 250);
            }
        }

        Item glory = EquipmentHelper.getChargedGlory();
        if(glory != null && !Equipment.contains(glory.getName())) {
            glory.click();
            TimeHelper.sleep(450, 850);
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
        Store.setAction("Finishing Lost City.");
        // Teleport out of cave.
        if(Players.getLocal().getPosition().getY() > 9000) {

            Item branch = Inventory.getFirst("Dramen branch");
            if(branch != null) {
                Inventory.use(s -> s.getName().equals("Knife"), branch);
                TimeHelper.sleep(450, 850);
                return;
            }
            int count = Inventory.getCount("Dramen staff");
            if(count < 5) {
                int diff = 5 - count;
                if(Inventory.getFreeSlots() < diff) {
                    Item lobster = Inventory.getFirst("Lobster");
                    lobster.interact("Drop");
                    TimeHelper.sleep(250, 450);
                    return;
                }
                SceneObject tree = SceneObjects.getNearest("Dramen tree");
                if(tree == null) {
                    Store.setAction("Failed to find dramen tree after killing spirit.");
                    return;
                }
                InteractHelper.interact(tree, "Chop down");
                TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), 1500);
                return;
            }

            Item glory = EquipmentHelper.getChargedGlory();
            if(glory != null) {
                if(Equipment.contains(s -> s.getName().equals(glory.getName()))) {
                    EquipmentSlot.NECK.interact("Draynor Village");
                    TimeHelper.sleep(850, 1880);
                }
                return;
            }
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            TimeHelper.sleep(5500, 6500);
            return;
        }

        if(!Equipment.contains("Dramen staff") && Inventory.contains("Dramen staff")) {
            Inventory.getFirst("Dramen staff").click();
            TimeHelper.sleep(230, 330);
        }

        Store.setAction("Walking to finish.");
        ZanarisHelper.goToZanaris(false);
    }

    private void goToEntrana() {
        if (!Area.rectangular(2790, 3398, 2880, 3323).setIgnoreFloorLevel(true).contains(Players.getLocal())) {
            Store.setAction("Going to Entrana.");
            Position monkTile = new Position(3042, 3241, 0);
            if (!monkTile.isLoaded() || monkTile.distance() > 10) {
                Logger.fine("Walking to monks tile.");
                MovementHelper.walkRandomized(monkTile, false);
                TimeHelper.sleep(560, 850);
                return;
            }
            Npc monk = Npcs.getNearest("Monk of Entrana");
            if (monk == null) {
                Logger.fine("Failed to find Monk to Entrana.");
                Store.setAction("Failed to find Monk to Entrana.");
                return;
            }
            InteractHelper.interact(monk, "Take-boat");
            TimeHelper.sleep(850, 1120);
            return;
        }

        if(Players.getLocal().getPosition().getFloorLevel() == 1) {
            Logger.fine("Checking if there is a plank to cross.");
            SceneObject plank = SceneObjects.getFirstAt(new Position(2834, 3333, 1));
            if(plank != null) {
                Store.setAction("Crossing plank.");
                plank.click();
                TimeHelper.sleep(850, 1120);
                return;
            }
            else {
                Logger.fine("No plank found to cross.");
            }
        }
        //2848, 9754

        //Near ladder
        if (!Dialog.isOpen()) {
            Position ladderPostion = new Position(2821, 3374, 0);
            if (ladderPostion.distance() > 5) {
                Logger.fine("Walking to ladder.");
                MovementHelper.walkRandomized(ladderPostion, false);
                return;
            }
            Logger.fine("Climbing down ladder.");
            SceneObject ladder = SceneObjects.getNearest("Ladder");
            InteractHelper.interact(ladder, "Climb-down");
            TimeHelper.sleep(850, 1500);
        }
        DialogueHelper.process(null, "Well that is a risk");
    }

    private void handleCave() {
        Store.setAction("Handling cave.");
        // Get axe from zombie.
        if (!Inventory.contains(s -> s.getName().contains("axe"))) {

            Pickable axe = Pickables.getNearest(x -> x.getName().contains("axe"));

            if (axe != null && axe.isPositionInteractable()) {

                if(Inventory.isFull()) {
                    Item lobster = Inventory.getFirst("Lobster");
                    if(lobster == null) {
                        Store.setAction("Inventory is full, but no lobsters?");
                        return;
                    }
                    lobster.interact("Drop");
                    TimeHelper.sleep(350, 550);
                }
                Store.setAction("Trying to pickup axe.");
                axe.interact("Take");
                TimeHelper.sleep(850, 1950);
                return;
            }

            boolean inSafeZone = Players.getLocal().getPosition().equals(zombieSafeSpot);
            if (!inSafeZone) {
                Movement.setWalkFlag(zombieSafeSpot);
                TimeHelper.sleep(300, 550);
                return;
            }
            Npc zombie = Npcs.getNearest(s ->
                    s.getName().equals("Zombie") &&
                            (s.getTarget() == null || s.getTarget().equals(Players.getLocal())) && s.distance() <= farthestSpot.distance());
            if (zombie == null) {
                Store.setAction("Waiting for zombie.");
                return;
            }
            Spell spell = getBestSpell();
            Magic.cast(spell, zombie);
            TimeHelper.sleep(300, 450);
            return;
        }

        final Npc spirit = Npcs.getNearest("Tree spirit");

        if(spirit == null) {
            if(!Players.getLocal().getPosition().equals(treeSpot)) {
                Movement.setWalkFlag(treeSpot);
                return;
            }
            SceneObject tree = SceneObjects.getNearest("Dramen tree");
            if(tree == null) {
                Store.setAction("Failed to find dramen tree and spirit.");
                return;
            }
            if(!tree.interact("Chop down")) {
                TimeHelper.sleep(100, 250);
            }
            TimeHelper.sleep(50, 100);
            return;
        }

        if(!Players.getLocal().getPosition().equals(treeSafeSpot)) {
            Movement.setWalkFlag(treeSafeSpot);
            TimeHelper.sleep(50, 100);
            return;
        }

        Magic.cast(getBestSpell(), spirit);
        TimeHelper.sleep(100, 250);

    }

    private void startQuest() {
        Store.setAction("Starting Lost City.");
        Position startTile = new Position(3148, 3205, 0);
        if (!startTile.isLoaded() || startTile.distance() > 10) {
            Store.setAction("Walking to start tile.");
            MovementHelper.walkRandomized(startTile, false);
            return;
        }
        DialogueHelper.process("Warrior", "What are you camped", "Who's Zanaris", "If it's hidden", "Looks like you don't");
    }

    private void interactTree() {
        if(!Inventory.contains(s -> s.getName().contains("axe") && !s.isNoted())) {
            Logger.info("Depositing everything except an adamant axe.");
            BankHelper.withdrawOnly(BankLocation.DRAYNOR, true, new ItemPair("Adamant axe", 1));
            TimeHelper.sleep(230,650);
            return;
        }
        Npc shamus = Npcs.getNearest("Shamus");
        Logger.fine("Looking for Shamus.");
        Store.setAction("Looking for Shamus.");
        if (shamus == null) {
            Position treePosition = new Position(3139, 3211, 0);
            if(treePosition.distance() > 5) {
                Logger.fine("Walking closer to lost city tree.");
                MovementHelper.walkRandomized(treePosition, false);
                return;
            }
            SceneObject tree = SceneObjects.getNearest(s -> s.getName().equals("Tree") && s.containsAction("Chop") && treePosition.distance(s.getPosition()) < 10);
            Logger.fine("Looking for tree to chop down.");
            if (tree == null) {
                Store.setAction("Tree not found, walking to known tree position.");
                MovementHelper.walkRandomized(new Position(3139, 3211, 0), false);
                TimeHelper.sleep(230, 545);
                return;
            }
            Store.setAction("Chopping Lost City Tree.");
            if(tree.distance() > 5) {
                Logger.fine("Walking closer to lost city tree.");
                MovementHelper.walkRandomized(tree.getPosition(), false);
                return;
            }
            Logger.fine("Interacting with tree.");
            InteractHelper.interact(tree, "Chop");
            TimeHelper.sleep(1200, 1800);
            return;
        }
        Logger.fine("Shamus has been found. Executing dialogue.");
        DialogueHelper.processWithEntity(shamus, "I've been in that shed");
    }

    private boolean withdrawItems(int varp) {

        Logger.fine("Checking need to withdraw items.");

        if (varp >= 5 || varp < 2 || Players.getLocal().getPosition().getY() > 9000) {
            Logger.fine("Not to Entrana stage yet or inside cave, not withdrawing items.");
            return true;
        }

        Logger.fine("Withdrawing items for entrana.");

        boolean goingToEntrana = varp == 2;

        if (goingToEntrana) {
            Predicate<Item> keep = s -> s.getName().contains("rune")
                    || s.getName().equals("Knife")
                    || s.getName().contains("Amulet of glory")
                    || s.getName().equals("Lobster") && !s.isNoted();
            if(Inventory.containsAnyExcept(keep)) {
                if (!BankHelper.depositAllExcept(BankLocation.DRAYNOR, keep)) {
                    return false;
                }
            }
        }

        if (!goingToEntrana) {
            Predicate<Item> keep = s -> s.getName().contains("rune")
                    || s.getName().equals("Knife")
                    || s.getName().equals("Adamant axe")
                    || s.getName().contains("Amulet of glory")
                    || s.getName().equals("Lobster");
            if(Inventory.containsAnyExcept(keep)) {
                BankHelper.depositAllExcept(BankHelper.nearest(), keep);
            }
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

    private Spell getBestSpell() {
        int magic = Skills.getCurrentLevel(Skill.MAGIC);
        if(magic < 5 && Inventory.contains("Air rune")) {
            return Spell.Modern.WIND_STRIKE;
        }
        if(magic < 9 && Inventory.contains("Water rune")) {
            return Spell.Modern.WATER_STRIKE;
        }
        if(magic < 13 && Inventory.contains("Earth rune")) {
            return Spell.Modern.EARTH_STRIKE;
        }
        if(magic >= 13 && Inventory.contains("Fire rune")) {
            return Spell.Modern.EARTH_STRIKE;
        }
        return Spell.Modern.WIND_STRIKE;
    }

    @Override
    public void notify(RenderEvent e) {
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
            return;
        }
        if(!valid) {
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

    @Override
    public void notify(Task task, Task task1) {
        if(!task1.equals(this)) {
            valid = false;
        }
    }
}
