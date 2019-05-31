package org.maddev.tasks;

import org.maddev.Config;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.crafting.CraftingHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.GrandExchangePurchaser;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskChangeListener;
import org.rspeer.ui.Log;

public class GrandExchange extends Task implements TaskChangeListener {

    private GrandExchangePurchaser purchaser;

    @Override
    public boolean validate() {
        if (Skills.getCurrentLevel(Skill.CRAFTING) < Config.CRAFTING_REQUIRED) {
            if(!hasCraftingSupplies()) return true;
        }
        if (Skills.getCurrentLevel(Skill.WOODCUTTING) < Config.WOODCUTTING_REQUIRED) {
            if(!hasWoodcuttingSupplies()) return true;
        }
        if (Skills.getCurrentLevel(Skill.HUNTER) < Config.HUNTING_REQUIRED) {
            if(!hasHuntingSupplies()) return true;
        }
        if (!LostCity.isComplete()) {
            if(!hasLostCitySupplies()) return true;
        }
        return false;
    }

    @Override
    public int execute() {
        if (walkTo()) {
            return Random.nextInt(350, 550);
        }

        if (!Inventory.contains("Coins")) {
            if(Inventory.isFull()) {
                Bank.depositInventory();
                Time.sleep(850, 1150);
            }
            BankHelper.withdrawAll("Coins");
            return Random.nextInt(350, 550);
        }

        if (Skills.getCurrentLevel(Skill.CRAFTING) < CraftingHelper.REQUIRED_LEVEL && !hasCraftingSupplies()) {
            getCraftingSupplies();
            return Random.nextInt(350, 550);
        }

        if (Skills.getCurrentLevel(Skill.WOODCUTTING) < 36 && !hasWoodcuttingSupplies()) {
            getWoodcuttingSupplies();
            return Random.nextInt(350, 550);
        }

        if (Skills.getCurrentLevel(Skill.HUNTER) < 17 && !hasHuntingSupplies()) {
            getHuntingSupplies();
        }

        if (!LostCity.isComplete() && !hasLostCitySupplies()) {
            getLostCitySupplies();
        }

        return Random.nextInt(350, 550);
    }

    private boolean walkTo() {
        Position p = BankLocation.GRAND_EXCHANGE.getPosition();
        if (p.distance() > 10) {
            return MovementHelper.walkRandomized(BankLocation.GRAND_EXCHANGE.getPosition(), false);
        }
        return false;
    }

    private boolean getCraftingSupplies() {
        Log.fine("Getting crafting supplies.");

        int leatherNeeded = CraftingHelper.getQuantityNeeded(7, CraftingHelper.LEATHER_GLOVES_XP)
                + CraftingHelper.getQuantityNeeded(10, CraftingHelper.LEATHER_BOOTS_XP);
        int threadCount = (leatherNeeded / CraftingHelper.SPOOL_PER_ITEM) + 1;
        int flaxCount = CraftingHelper.getQuantityNeeded(31, CraftingHelper.FLAX_XP);

        if (purchaser != null && (!purchaser.hasItem("Leather") && !purchaser.hasItem("Flax"))) {
            purchaser = null;
        }

        if (purchaser == null) {
            purchaser = new GrandExchangePurchaser();
            ItemPair leather = new ItemPair("Leather", leatherNeeded, 2);
            ItemPair thread = new ItemPair("Thread", threadCount, 2);
            ItemPair needle = new ItemPair("Needle", 1, 3);
            ItemPair flax = new ItemPair("Flax", flaxCount, 2);
            if(Skills.getCurrentLevel(Skill.CRAFTING) < 10) {
                purchaser.addItem(leather);
                purchaser.addItem(thread);
                purchaser.addItem(needle);
            }
            else {
                purchaser.addItem(flax);
            }
        }
        return purchaser.purchase();
    }

    private boolean getWoodcuttingSupplies() {
        Log.fine("Attempting to purchase woodcutting supplies.");
        // Clear from crafting purchasing.
        if (purchaser != null && (purchaser.hasItem("Leather") || purchaser.hasItem("Flax"))) {
            purchaser = null;
        }

        if (purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Iron axe", 1, 5, 1000),
                    new ItemPair("Steel axe", 1, 5, 1000),
                    new ItemPair("Mithril axe", 1, 10),
                    new ItemPair("Adamant axe", 1, 10));
        }

        return purchaser.purchase();
    }

    private boolean getLostCitySupplies() {
        Log.fine("Attempting to purchase lost city supplies.");
        // Clear from crafting purchasing.
        if (purchaser != null && !purchaser.hasItem("Mind rune")) {
            purchaser = null;
        }
        if (purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Knife", 1, 5),
                    new ItemPair("Mind rune", 400, 5),
                    new ItemPair("Air rune", 700, 5),
                    new ItemPair("Earth rune", 300, 5),
                    new ItemPair("Water rune", 300, 5),
                    new ItemPair("Fire rune", 200, 5),
                    new ItemPair("Lobster", 15, 5)
            );
            if (!PlayerHelper.hasAny(EquipmentHelper.getChargedGlories())) {
                purchaser.addItem(new ItemPair("Amulet of glory(6)", 1, 3));
            }
        }

        return purchaser.purchase();
    }

    private boolean getHuntingSupplies() {
        Log.fine("Attempting to purchase hunting supplies.");
        // Clear from crafting purchasing.
        if (purchaser != null && !purchaser.hasItem("Bird snare")) {
            purchaser = null;
        }

        if(purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Bird snare", 3, 5)
            );
            if (!PlayerHelper.hasAny(EquipmentHelper.getGamesNecklaces())) {
                purchaser.addItem(new ItemPair("Games necklace(8)", 1, 5));
            }
        }

        return purchaser.purchase();
    }

    private boolean hasCraftingSupplies() {
        if (Skills.getCurrentLevel(Skill.CRAFTING) < 10) {
            return PlayerHelper.hasAll("Leather", "Thread", "Needle");
        }
        return PlayerHelper.hasAll("Flax");
    }


    private boolean hasWoodcuttingSupplies() {
        return PlayerHelper.hasAll("Iron axe", "Steel axe", "Mithril axe", "Adamant axe");
    }

    private boolean hasHuntingSupplies() {
        return PlayerHelper.hasAll("Bird snare") && PlayerHelper.hasAny(EquipmentHelper.getGamesNecklaces());
    }

    private boolean hasLostCitySupplies() {
        boolean hasRunes = PlayerHelper.getTotalCount("Mind rune") >= 400;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Air rune") >= 700;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Earth rune") >= 300;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Water rune") >= 300;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Fire rune") >= 200;
        if (!hasRunes) {
            Log.fine("Does not have all the runes!");
            return false;
        }
        return PlayerHelper.hasAll("Knife")
                && PlayerHelper.getTotalCount("Lobster") >= 15
                && PlayerHelper.hasAny(EquipmentHelper.getChargedGlories());
    }

    @Override
    public void notify(Task task, Task curr) {
        if (this.equals(curr)) {
            Log.fine("We are now active.");
        } else {
            Log.fine("We are not active.");
        }
    }
}
