package org.maddev.helpers.grand_exchange;

import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.walking.CustomWalker;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;

public class GrandExchangeHelper {

    public static boolean walkTo() {
        Store.setAction("Walking to Grand Exchange.");
        if(ZanarisHelper.inZanaris()) {
            if(BankCache.contains("Varrock teleport") && !Inventory.contains("Varrock teleport")) {
                if(!BankHelper.withdraw("Varrock teleport", 1)) {
                    return false;
                }
            }
            Item teleport = Inventory.getFirst("Varrock teleport");
            if(teleport != null) {
                teleport.click();
                Time.sleep(850, 1150);
                return false;
            }
            if(CustomWalker.canUseHomeTeleport() && !Players.getLocal().isAnimating()) {
                Magic.cast(Spell.Modern.HOME_TELEPORT);
                Time.sleepUntil(() -> Players.getLocal().isAnimating(), 2500);
                return false;
            }
            else {
                Store.setAction("Waiting until we can home teleport.");
                return false;
            }
        }
        Position p = BankLocation.GRAND_EXCHANGE.getPosition();
        if (p.distance() > 10) {
            return MovementHelper.walkRandomized(BankLocation.GRAND_EXCHANGE.getPosition(), false);
        }
        return false;
    }

    public static boolean open() {
        if (GrandExchange.isOpen()) {
            return true;
        }
        if(walkTo()) {
           return false;
        }
        Store.setAction("Opening Grand Exchange");
        if (Bank.isOpen()) {
            Bank.close();
        }
        Npc clerk = Npcs.getNearest("Grand Exchange Clerk");
        if (clerk != null) {
            clerk.interact("Exchange");
            Time.sleep(230, 750);
            Time.sleepUntil(GrandExchange::isOpen, 2500);
        }
        return false;
    }
}
