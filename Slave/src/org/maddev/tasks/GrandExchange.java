package org.maddev.tasks;

import org.maddev.Config;
import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.crafting.CraftingHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.GrandExchangePurchaser;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.grand_exchange.PriceChecker;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.time.TimeHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.providers.RSItemDefinition;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.io.IOException;

public class GrandExchange extends Task {

    private GrandExchangePurchaser purchaser;

    private static int essenceQuantity;
    private static int eclecticQuantity;
    private static int natureQuantity;
    private static boolean purchaserActive;

    public static boolean isPurchaserActive() {
        return purchaserActive;
    }

    private boolean needsToPurchase() {
        if (Skills.getCurrentLevel(Skill.CRAFTING) < Config.CRAFTING_REQUIRED) {
            if (!hasCraftingSupplies()) {
                Logger.fine("Need to purchase crafting supplies.");
                Store.setAction("Getting Crafting Supplies.");
                return true;
            }
        }
        if (Skills.getCurrentLevel(Skill.WOODCUTTING) < Config.WOODCUTTING_REQUIRED) {
            if (!hasWoodcuttingSupplies()) {
                Logger.fine("Need to purchase woodcutting supplies.");
                Store.setAction("Getting Woodcutting Supplies.");
                return true;
            }
        }
        if (Skills.getCurrentLevel(Skill.HUNTER) < Config.HUNTING_REQUIRED) {
            if (!hasHuntingSupplies()) {
                Logger.fine("Need to purchase hunting supplies.");
                Store.setAction("Getting Hunting Supplies.");
                return true;
            }
        }
        if (LostCity.mayNeedSupplies()) {
            if (!hasLostCitySupplies()) {
                Logger.fine("Need to purchase lost city supplies.");
                Store.setAction("Getting Lost City Supplies.");
                return true;
            }
        }
        if (!ZanarisHelper.inPuroPuro() && !hasImplings()) {
            Logger.fine("Need to purchase impling supplies.");
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() {
        if(purchaser != null) {
            return true;
        }
        if (needsToPurchase()) {
            return true;
        }
        if (Inventory.contains("Coins") && !needsToPurchase()) {
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        Store.setTask("Grand Exchange");

        purchaserActive = purchaser != null;
        if(purchaser != null) {
            log("Purchase is still active, continuing purchasing.");
            boolean success = purchaser.purchase();
            if(success) {
                log("Purchasing was successful.");
                purchaser = null;
            }
            return Random.nextInt(350, 550);
        }

        if (Inventory.contains("Coins") && !needsToPurchase()) {
            depositCoins();
            return Random.nextInt(350, 550);
        }

        if (!Inventory.contains("Coins") || BankCache.contains("Coins")) {
            if (Inventory.isFull()) {
                Store.setAction("Depositing inventory.");
                Bank.depositInventory();
                TimeHelper.sleep(850, 1150);
            }
            Logger.fine("Opening bank to get coins.");
            BankHelper.withdrawAll("Coins", BankLocation.GRAND_EXCHANGE, true);
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

        if (!hasImplings()) {
            try {
                getImplings();
            } catch (IOException e) {
                Logger.severe("Failed to calculate impling prices. Can not buy.");
                e.printStackTrace();
            }
            return Random.nextInt(350, 550);
        }

        return Random.nextInt(350, 550);
    }

    private void depositCoins() {
        Store.setAction("Depositing coins.");
        BankHelper.depositAllExcept(BankLocation.GRAND_EXCHANGE, s -> false);
    }


    private boolean getCraftingSupplies() {
        Store.setAction("Getting crafting supplies.");

        int leatherNeeded = CraftingHelper.getQuantityNeeded(7, CraftingHelper.LEATHER_GLOVES_XP)
                + CraftingHelper.getQuantityNeeded(10, CraftingHelper.LEATHER_BOOTS_XP);
        int threadCount = (leatherNeeded / CraftingHelper.SPOOL_PER_ITEM) + 1;
        int flaxCount = CraftingHelper.getQuantityNeeded(31, CraftingHelper.FLAX_XP);

        if (purchaser != null && (!purchaser.hasItem("Leather") && !purchaser.hasItem("Flax"))) {
            log("Crafting - Clearing Purchaser");
            purchaser = null;
        }

        if (purchaser == null) {
            log("Crafting - Setting Purchaser");
            purchaser = new GrandExchangePurchaser();
            ItemPair leather = new ItemPair("Leather", leatherNeeded, 2);
            ItemPair thread = new ItemPair("Thread", threadCount, 2);
            ItemPair needle = new ItemPair("Needle", 1, 10);
            ItemPair flax = new ItemPair("Flax", flaxCount, 2);
            if (Skills.getCurrentLevel(Skill.CRAFTING) < 10) {
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
        if (purchaser != null && (!purchaser.hasItem("Iron axe"))) {
            log("Woodcutting - Clearing Purchaser");
            purchaser = null;
        }

        if (purchaser == null) {
            log("Woodcutting - Setting Purchaser");
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Iron axe", 1, 5, 1000),
                    new ItemPair("Steel axe", 1, 5, 1000),
                    new ItemPair("Mithril axe", 1, 20),
                    new ItemPair("Adamant axe", 1, 10));
        }

        return purchaser.purchase();
    }

    private boolean getLostCitySupplies() {
        Store.setAction("Purchasing lost city supplies.");
        // Clear from crafting purchasing.
        if (purchaser != null && !purchaser.hasItem("Mind rune")) {
            log("LostCity - Clearing Purchaser");
            purchaser = null;
        }
        if (purchaser == null) {
            log("LostCity - Setting Purchaser");
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

        if (purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Bird snare", 3, 20)
            );
            if (!PlayerHelper.hasAny(EquipmentHelper.getGamesNecklaces())) {
                purchaser.addItem(new ItemPair("Games necklace(8)", 1, 5));
            }
        }

        return purchaser.purchase();
    }

    private boolean getImplings() throws IOException {
        Store.setAction("Getting implings.");
        if (purchaser != null && !purchaser.hasItem("Essence impling") && !purchaser.hasItem("Eclectic impling jar") && !purchaser.hasItem("Nature impling jar")) {
            log("GetImplings - Clearing Purchaser");
            purchaser = null;
        }
        if (purchaser == null) {
            log("GetImplings - Setting Purchaser");
            ItemPair[] items = calculateImplingsQuantity();
            purchaser = new GrandExchangePurchaser(
                    items
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
        if (!LostCity.isComplete()) {
            boolean triedToGetAll = PlayerHelper.hasAllIncludeGrandExchange(true,
                    "Essence impling jar", "Eclectic impling jar", "Nature impling jar");
            if (triedToGetAll) {
                return true;
            }
        }
        return PlayerHelper.hasAny("Jar generator") || (ZanarisHelper.hasRequiredItems() && ZanarisHelper.hasTeleports());
    }

    private ItemPair[] calculateImplingsQuantity() throws IOException {
        ItemPair[] items = new ItemPair[5];
        RSItemDefinition essenceDef = Definitions.getItem("Essence impling jar", s -> !s.isNoted());
        RSItemDefinition eclecticDef = Definitions.getItem("Eclectic impling jar", s -> !s.isNoted());
        RSItemDefinition natureDef = Definitions.getItem("Nature impling jar", s -> !s.isNoted());

        int essencePrice = PriceChecker.getOSBuddyPrice(essenceDef.getId());
        int eclecticPrice = PriceChecker.getOSBuddyPrice(eclecticDef.getId());
        int naturePrice = PriceChecker.getOSBuddyPrice(natureDef.getId());

        Logger.fine("Essence Prices: " + essencePrice + " " + eclecticPrice + " " + naturePrice);

        essencePrice = (int) (essencePrice + (essencePrice * 0.10));
        eclecticPrice = (int) (eclecticPrice + (eclecticPrice * 0.10));
        naturePrice = (int) (naturePrice + (naturePrice * 0.10));

        int essenceMultiplier = 3;
        int eclecticMultiplier = 2;
        int natureMultiplier = 1;

        Logger.fine("Essence Prices After: " + essencePrice + " " + eclecticPrice + " " + naturePrice);

        int coins = Inventory.getCount(true, "Coins");

        double count = (coins * .95) / ((essenceMultiplier * essencePrice) + (eclecticMultiplier * eclecticPrice) + (natureMultiplier * naturePrice));

        essenceQuantity = (int) Math.floor(count * essenceMultiplier) - PlayerHelper.getTotalCount("Essence impling jar");
        eclecticQuantity = (int) Math.floor(count * eclecticMultiplier) - PlayerHelper.getTotalCount("Eclectic impling jar");
        natureQuantity = (int) Math.floor(count * natureMultiplier) - PlayerHelper.getTotalCount("Nature impling jar");

        Logger.info("Buying Essence Quantities: " + essenceQuantity + " " + eclecticQuantity + " " + natureQuantity);

        items[0] = new ItemPair("Essence impling jar", essenceQuantity, 1);
        items[1] = new ItemPair("Eclectic impling jar", eclecticQuantity, 1);
        items[2] = new ItemPair("Nature impling jar", natureQuantity, 1);
        items[0].setPrice(essencePrice);
        items[1].setPrice(eclecticPrice);
        items[2].setPrice(naturePrice);

        if (!ZanarisHelper.hasTeleports()) {
            Log.info("Adding varrock teleport to items.");
            items[3] = new ItemPair("Varrock teleport", 10, 6);
            Log.info("Adding lumbridge teleport to items.");
            items[4] = new ItemPair("Lumbridge teleport", 10, 6);
        }

        return items;
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

    protected void log(String message) {
        Logger.fine("GrandExchangeTask", message);
    }

    public static int getEclecticQuantity() {
        return eclecticQuantity;
    }

    public static int getEssenceQuantity() {
        return essenceQuantity;
    }

    public static int getNatureQuantity() {
        return natureQuantity;
    }
}
