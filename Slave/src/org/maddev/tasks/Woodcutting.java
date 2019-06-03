package org.maddev.tasks;

import org.maddev.Config;
import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.util.function.Predicate;

public class Woodcutting extends Task {

    public static final Position TREES = new Position(2969, 3197);

    @Override
    public boolean validate() {
        return Skills.getCurrentLevel(Skill.WOODCUTTING) < Config.WOODCUTTING_REQUIRED;
    }

    @Override
    public int execute() {
        Store.setTask("Woodcutting.");
        int loop = Random.nextInt(350, 850);
        if(!BankHelper.withdrawOnly(BankLocation.DRAYNOR, true,
                new ItemPair("Iron axe", 1),
                new ItemPair("Steel axe", 1),
                new ItemPair("Mithril axe", 1),
                new ItemPair("Adamant axe", 1))) {
            return loop;
        }

        if(Players.getLocal().getAnimation() != -1) {
            return loop;
        }

        if(TREES.distance() > 50) {
            Store.setAction("Walking to trees.");

            MovementHelper.walkRandomized(TREES, false, TREES.distance() > 300);
            return loop;
        }

        if(Inventory.isFull()) {
            Item[] logs = Inventory.getItems(s -> s.getName().toLowerCase().contains("logs"));
            for (Item log : logs) {
                log.interact("Drop");
                Time.sleep(350, 750);
            }
            return loop;
        }

        Store.setAction("Woodcutting.");
        SceneObject tree = SceneObjects.getNearest(TREE);

        if(tree == null) {
            Store.setAction("Waiting for tree.");
            return loop;
        }

        InteractHelper.interact(tree, "Chop down");
        Time.sleepUntil(() -> Players.getLocal().isAnimating(), 3500);
        return loop;
    }

    private Predicate<SceneObject> TREE = s -> {
        String name = getTreeName();
        return s.getName().equals(name) && s.distance(TREES) < 40;
    };

    private String getTreeName() {
        int level = Skills.getCurrentLevel(Skill.WOODCUTTING);
        if(level < 15) return "Tree";
        if(level < 30) return "Oak";
        return "Willow";
    }
}

