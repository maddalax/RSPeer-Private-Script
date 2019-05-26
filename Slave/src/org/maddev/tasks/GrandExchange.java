package org.maddev.tasks;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.crafting.CraftingHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.grand_exchange.GrandExchangePurchaser;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskChangeListener;

public class GrandExchange extends Task implements TaskChangeListener {

    private GrandExchangePurchaser purchaser;

    @Override
    public boolean validate() {
        boolean any = false;
        if(Skills.getCurrentLevel(Skill.CRAFTING) < 31) {
            any = !hasCraftingSupplies();
        }
        if(Skills.getCurrentLevel(Skill.WOODCUTTING) < 36) {
            any = !hasWoodcuttingSupplies();
        }
        return any || !hasLostCitySupplies();
    }

    @Override
    public int execute() {
        if (walkTo()) {
            return Random.nextInt(350, 550);
        }

        if(!Inventory.contains("Coins")) {
            BankHelper.withdrawAll("Coins");
            return Random.nextInt(350, 550);
        }

        if(Skills.getCurrentLevel(Skill.CRAFTING) < CraftingHelper.REQUIRED_LEVEL && !hasCraftingSupplies()) {
            getCraftingSupplies();
            return Random.nextInt(350, 550);
        }

        if(Skills.getCurrentLevel(Skill.WOODCUTTING) < 36 && !hasWoodcuttingSupplies()) {
            getWoodcuttingSupplies();
            return Random.nextInt(350, 550);
        }

        getLostCitySupplies();

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
        System.out.println("Getting crafting supplies.");

        int leatherNeeded = CraftingHelper.getQuantityNeeded(7, CraftingHelper.LEATHER_GLOVES_XP)
                + CraftingHelper.getQuantityNeeded(10, CraftingHelper.LEATHER_BOOTS_XP);
        int thread = (leatherNeeded / CraftingHelper.SPOOL_PER_ITEM) + 1;
        int flax = CraftingHelper.getQuantityNeeded(31, CraftingHelper.FLAX_XP);

        if(purchaser == null) {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Leather", leatherNeeded, 2),
                    new ItemPair("Thread", thread, 2),
                    new ItemPair("Needle", 1, 3),
                    new ItemPair("Flax", flax, 2));
        }
        return purchaser.purchase();
    }

    private boolean getWoodcuttingSupplies() {
        System.out.println("Attempting to purchase woodcutting suppies.");
        // Clear from crafting purchasing.
        if(purchaser != null && purchaser.hasItem("Leather")) {
            purchaser.clear();
        }
        else {
            purchaser = new GrandExchangePurchaser(new ItemPair("Iron axe", 1, 5, 1000),
                    new ItemPair("Steel axe", 1, 5, 1000),
                    new ItemPair("Mithril axe", 1, 10),
                    new ItemPair("Adamant axe", 1, 10));
        }

        return purchaser.purchase();
    }

    private boolean getLostCitySupplies() {
        System.out.println("Attempting to purchase lost city supplies.");
        // Clear from crafting purchasing.
        if(purchaser != null && !purchaser.hasItem("Mind rune")) {
            purchaser.clear();
        }
        else {
            purchaser = new GrandExchangePurchaser(
                    new ItemPair("Knife", 1, 5),
                    new ItemPair("Mind rune", 400, 5),
                    new ItemPair("Air rune", 700, 5),
                    new ItemPair("Earth rune", 300, 5),
                    new ItemPair("Water rune", 300, 5),
                    new ItemPair("Lobster", 15, 5)
            );
            if(!PlayerHelper.hasAny(EquipmentHelper.getChargedGlories())) {
                purchaser.addItem(new ItemPair("Amulet of glory(6)", 1, 3));
            }
        }

        return purchaser.purchase();
    }

    private boolean hasCraftingSupplies() {
        return PlayerHelper.hasAll("Leather", "Thread", "Needle");
    }


    private boolean hasWoodcuttingSupplies() {
        return PlayerHelper.hasAll("Iron axe", "Steel axe", "Mithril axe", "Adamant axe");
    }

    private boolean hasHuntingSupplies() {
        return PlayerHelper.hasAll("Games ");
    }

    private boolean hasLostCitySupplies() {
        boolean hasRunes = PlayerHelper.getTotalCount("Mind rune") >= 400;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Air rune") >= 700;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Earth rune") >= 300;
        hasRunes = hasRunes && PlayerHelper.getTotalCount("Water rune") >= 300;
        if(!hasRunes) {
            System.out.println("Does not have all the runes!");
            return false;
        }
        return PlayerHelper.hasAll("Knife", "Lobster")
                && PlayerHelper.hasAny(EquipmentHelper.getChargedGlories());
    }

    @Override
    public void notify(Task task, Task curr) {
        if (this.equals(curr)) {
            System.out.println("We are now active.");
        } else {
            System.out.println("We are not active.");
        }
    }
}
