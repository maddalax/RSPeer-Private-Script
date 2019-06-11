package org.maddev.helpers.grand_exchange;

import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.CustomWalker;
import org.maddev.helpers.walking.MovementHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.BankLocation;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.maddev.helpers.log.Logger;

public class GrandExchangeHelper {

    public static boolean walkTo() {
        Store.setAction("Walking to Grand Exchange.");
        if(ZanarisHelper.inZanaris()) {
            log("In zanaris, attempting to teleport via varrock teleport or home teleport.");
            if(BankCache.contains("Varrock teleport") && !Inventory.contains("Varrock teleport")) {
                log("We have varrock teleport, withdrawing.");
                if(!BankHelper.withdraw("Varrock teleport", 1)) {
                    log("Failed to withdraw varrock teleport.");
                    return false;
                }
            }
            Item teleport = Inventory.getFirst("Varrock teleport");
            if(teleport != null) {
                log("Clicking varrock teleport.");
                teleport.click();
                TimeHelper.sleep(850, 1150);
                return false;
            }
            if(MovementHelper.getInstance().canUseHomeTeleport() && !Players.getLocal().isAnimating()) {
                log("Casting home teleport.");
                Magic.cast(Spell.Modern.HOME_TELEPORT);
                TimeHelper.sleepUntil(() -> Players.getLocal().isAnimating(), 2500);
                return false;
            }
            else {
                log("We can not home teleport yet, waiting.");
                Store.setAction("Waiting until we can home teleport.");
                return false;
            }
        }
        Position p = BankLocation.GRAND_EXCHANGE.getPosition();
        if (p.distance() > 10) {
            log("Walking to Grand Exchange position: " + p.toScreen());
            return MovementHelper.walkRandomized(p, false);
        }
        return false;
    }

    public static boolean open() {
        if (GrandExchange.isOpen()) {
            log("Grand exchange is open.");
            return true;
        }
        Store.setAction("Opening Grand Exchange");
        if (Bank.isOpen()) {
            log("Closing bank.");
            Bank.close();
        }
        Npc clerk = Npcs.getNearest("Grand Exchange Clerk");
        if(clerk == null) {
            log("Grand Exchange Clerk was null, walking to.");
            walkTo();
            return false;
        }
        InteractHelper.interact(clerk, "Exchange");
        TimeHelper.sleep(230, 750);
        TimeHelper.sleepUntil(GrandExchange::isOpen, 2500);
        return GrandExchange.isOpen();
    }

    private static void log(String message) {
        Logger.fine("GrandExchangeHelper", message);
    }

    public static RSGrandExchangeOffer getBuyOffer(String name) {
        return getOffer(name, RSGrandExchangeOffer.Type.BUY);
    }

    public static RSGrandExchangeOffer getSellOffer(String name) {
        return getOffer(name, RSGrandExchangeOffer.Type.SELL);
    }

    public static RSGrandExchangeOffer getOffer(String name, RSGrandExchangeOffer.Type type) {
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers(s ->
                s.getType() == type &&
                        s.getItemDefinition() != null &&
                        s.getItemDefinition().getName().toLowerCase().equals(name.toLowerCase()));
        if (offers.length == 0) {
            return null;
        }
        return offers[0];
    }
}
