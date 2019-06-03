package org.maddev.tasks;

import org.maddev.Config;
import org.maddev.Store;
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

public class GrandExchange extends Task {

    private GrandExchangePurchaser purchaser;

    @Override
    public boolean validate() {
        if (Skills.getCurrentLevel(Skill.CRAFTING) < Config.CRAFTING_REQUIRED) {
            if(!hasCraftingSupplies()) {
                Store.setAction("Getting Crafting Supplies.");
                return true;
            }
        }
        if (Skills.getCurrentLevel(Skill.WOODCUTTING) < Config.WOODCUTTING_REQUIRED) {
            if(!hasWoodcuttingSupplies()) {
                Store.setAction("Getting Woodcutting Supplies.");
                return true;
            }
        }
        if (Skills.getCurrentLevel(Skill.HUNTER) < Config.HUNTING_REQUIRED) {
            if(!hasHuntingSupplies()) {
                Store.setAction("Getting Hunting Supplies.");
                return true;
            }
        }
        if (LostCity.mayNeedSupplies()) {
            if(!hasLostCitySupplies()) {
                Store.setAction("Getting Lost City Supplies.");
                return true;
            }
        }
        if(!hasImplings()) {
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        Store.setTask("Grand Exchange");
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

        if (Skills.getCurrentLevel(Skill.WOODCUTTING) < Config.WOODCUTTING_REQUIRED && !hasWoodcuttingSupplies()) {
            getWoodcuttingSupplies();
            return Random.nextInt(350, 550);
        }

        if (Skills.getCurrentLevel(Skill.HUNTER) < Config.HUNTING_REQUIRED && !hasHuntingSupplies()) {
            getHuntingSupplies();
            return Random.nextInt(350, 550);
        }

        if (LostCity.mayNeedSupplies() && !hasLostCitySupplies()) {
            getLostCitySupplies();
            return Random.nextInt(350, 550);
        }

        if(!hasImplings()) {
            getImplings();
            return Random.nextInt(350, 550);
        }

        return Random.nextInt(350, 550);
    }

    private boolean walkTo() {
        Store.setAction("Walking to Grand Exchange.");
        Position p = BankLocation.GRAND_EXCHANGE.getPosition();
        if (p.distance() > 10) {
            return MovementHelper.walkRandomized(BankLocation.GRAND_EXCHANGE.getPosition(), false);
        }
        return false;
    }

    private boolean getCraftingSupplies() {
        Store.setAction("Getting crafting supplies.");

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
            ItemPair needle = new ItemPair("Needle", 1, 10);
            ItemPair flax = new ItemPair("Flax", flaxCount, 2);
            if(Skills.getCurrentLevel(Skill.CRAFTING) < 10) {
                purchaser.addItem(leather);
                purchaser.addItem(thread);
                purchaser.addItem(needle);
            }
            purchaser.addItem(flax);
        }
        return purchaser.purchase();
    }

    private boolean getWoodcuttingSupplies() {
        Store.setAction("Purchasing woodcutting supplies.");
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
        Store.setAction("Purchasing lost city supplies.");
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
        Store.setAction("Purchase hunting supplies.");
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

    private boolean getImplings() {
        Store.setAction("Getting implings.");
        if (purchaser != null && !purchaser.hasItem("Essence impling")) {
            purchaser = null;
        }
        if(purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Essence impling jar", 3, 3),
                    new ItemPair("Eclectic impling jar", 3, 3),
                    new ItemPair("Nature impling jar", 3, 3)
            );
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

    private boolean hasImplings() {
        return PlayerHelper.hasAll("Essence impling jar", "Eclectic impling jar", "Nature impling jar");
    }

    private boolean hasLostCitySupplies() {
        boolean hasRunes = PlayerHelper.getTotalCount("Mind rune") >= 400;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Air rune") >= 700;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Earth rune") >= 300;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Water rune") >= 300;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Fire rune") >= 200;
        if (!hasRunes) {
            return false;
        }
        return PlayerHelper.hasAll("Knife")
                && PlayerHelper.getTotalCount("Lobster") >= 15
                && PlayerHelper.hasAny(EquipmentHelper.getChargedGlories());
    }
}
