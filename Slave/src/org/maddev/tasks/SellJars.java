package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.GrandExchangeHelper;
import org.maddev.helpers.grand_exchange.GrandExchangeSeller;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;

public class SellJars extends Task {

    private static final String jar = "Impling jar";

    public ItemPair toItemPair() {
        return new ItemPair(jar, Integer.MAX_VALUE, 2);
    }

    private GrandExchangeSeller seller;

    public SellJars() {
        this.seller = new GrandExchangeSeller(toItemPair());
    }
    @Override
    public boolean validate() {
        boolean hasJar = PlayerHelper.hasAny(jar);
        if(hasJar) {
            Logger.fine("Sell Jars", "We do have " + jar + ". Selling.");
        }
        return PlayerHelper.hasAny(jar) && !ZanarisHelper.hasRequiredItems()
                && !PlayerHelper.hasAny("Jar generator");
    }

    @Override
    public int execute() {
        int loop = Random.nextInt(450, 650);
        Store.setTask("Selling Jars");
        if(Inventory.contains(s -> !s.isNoted() && !s.getName().equals("Coins"))) {
            Logger.fine("Depositing Items");
            BankHelper.depositAllExcept(BankLocation.GRAND_EXCHANGE, s -> false);
            return loop;
        }
        if(!BankHelper.withdrawNoted(BankLocation.GRAND_EXCHANGE, true, toItemPair())) {
            Logger.fine("Withdrawing impling jars.");
            return loop;
        }
        if(!GrandExchangeHelper.open()) {
            Logger.fine("Opening Grand Exchange");
            return loop;
        }
        Logger.fine("Executing seller.");
        this.seller.sell();
        return loop;
    }


}
