package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
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
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
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
        boolean hasJarGen = PlayerHelper.hasAny("Jar generator");
        if(hasJar) {
            Logger.fine("Sell Jars", "We do have " + jar + ". Selling.");
        }
        if(!ZanarisHelper.inZanaris() && !ZanarisHelper.inPuroPuro() && !hasJarGen && hasJar) {
            return true;
        }
        RSGrandExchangeOffer offer = GrandExchangeHelper.getSellOffer(jar);
        if(offer != null) {
            return true;
        }
        return hasJar && !ZanarisHelper.hasRequiredItems()
                && !hasJarGen;
    }

    @Override
    public int execute() {
        int loop = Random.nextInt(450, 650);
        Store.setTask("Selling Jars");
        if(Inventory.contains(s -> s.getName().equals(jar) && !s.isNoted())) {
            Store.setAction("Depositing unnoted jars.");
            BankHelper.depositAllExcept(BankLocation.GRAND_EXCHANGE, s -> false);
            return loop;
        }
        if (BankCache.contains(jar)) {
            if(!BankHelper.withdrawNoted(BankLocation.GRAND_EXCHANGE, true, toItemPair())) {
                Store.setAction("Withdrawing impling jars.");
                Logger.fine("Withdrawing impling jars.");
                return loop;
            }
        }
        if(Inventory.contains(s -> !s.isNoted() && !s.getName().equals("Coins"))) {
            Store.setAction("Depositing all items except coins.");
            Logger.fine("Depositing all items except coins.");
            BankHelper.depositAllExcept(BankLocation.GRAND_EXCHANGE, s -> false);
            return loop;
        }
        RSGrandExchangeOffer offer = GrandExchangeHelper.getSellOffer(jar);
        if(offer != null && offer.getProgress() != RSGrandExchangeOffer.Progress.FINISHED) {
            Store.setAction("Waiting for jars to sell.");
            Logger.fine("Waiting for impling jars to sell.");
            return loop;
        }
        if(offer != null && offer.getProgress() == RSGrandExchangeOffer.Progress.FINISHED) {
            Store.setAction("Collecting sold jars.");
            Logger.fine("Jars are sold, collecting offer.");
            this.seller.collect();
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
