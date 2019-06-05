package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.grand_exchange.GrandExchangeHelper;
import org.maddev.helpers.grand_exchange.GrandExchangeSeller;
import org.maddev.helpers.grand_exchange.ItemPair;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.zanris.ZanarisHelper;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.script.task.Task;

import java.util.Arrays;

public class SellJars extends Task {

    public static final String[] items = new String[] {"Impling jar", "Butterfly jar", "Eclectic impling jar", "Essence impling jar", "Nature impling jar"};

    public ItemPair[] toItemPair() {
        return Arrays.stream(items).map(s -> new ItemPair(s, Integer.MAX_VALUE, 2)).toArray(ItemPair[]::new);
    }

    private GrandExchangeSeller seller;

    public SellJars() {
        this.seller = new GrandExchangeSeller(toItemPair());
    }

    @Override
    public boolean validate() {
        return PlayerHelper.hasAny(items) && !ZanarisHelper.hasRequiredItems();
    }

    @Override
    public int execute() {
        int loop = Random.nextInt(450, 650);
        Store.setTask("Selling Jars");
        ItemPair[] pairs = Arrays.stream(items).map(s -> new ItemPair(s, Integer.MAX_VALUE)).toArray(ItemPair[]::new);
        if(!BankHelper.withdrawNoted(BankLocation.GRAND_EXCHANGE, true, pairs)) {
            return loop;
        }
        if(!GrandExchangeHelper.open()) {
            return loop;
        }
        this.seller.sell();
        return loop;
    }


}
