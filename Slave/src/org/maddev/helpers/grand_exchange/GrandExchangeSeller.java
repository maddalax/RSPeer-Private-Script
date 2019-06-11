package org.maddev.helpers.grand_exchange;

import org.maddev.Store;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.maddev.helpers.log.Logger;

import java.util.concurrent.CopyOnWriteArrayList;

public class GrandExchangeSeller {

    private CopyOnWriteArrayList<ItemPair> pairs;

    public GrandExchangeSeller(ItemPair... pairs) {
        this.pairs = new CopyOnWriteArrayList<>(pairs);
    }

    public void addItem(ItemPair pair) {
        if (hasItem(pair.getName())) {
            return;
        }
        pairs.add(pair);
    }

    public void clear() {
        pairs.clear();
    }

    private boolean hasItem(String name) {
        for (ItemPair pair : pairs) {
            if (pair.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean sell(ItemPair pair) {

        if (!GrandExchangeHelper.open()) {
            log("Grand Exchange open returned false, not selling " + pair.getName() + " yet.");
            return false;
        }

        if (!GrandExchangeSetup.isOpen()) {
            log("Attempting to create offer for " + pair.getName() + ".");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL);
            TimeHelper.sleep(500, 1000);
            return false;
        }

        Store.setAction("Creating offer for " + pair.getName());

        if (GrandExchangeSetup.getItem() == null) {
            log("Attempting to create offer for" + pair.getName());
            GrandExchangeSetup.setItem(pair.getName());
            return false;
        }

        if (pair.getOriginalPrice() == 0) {
            pair.setOriginalPrice(GrandExchangeSetup.getPricePerItem());
        }

        if (pair.getPrice() != 0) {

            if (GrandExchangeSetup.getPricePerItem() != pair.getPrice()) {
                Store.setAction("Setting price to " + pair.getPrice() + " for " + pair.getName() + ".");
                GrandExchangeSetup.setPrice(pair.getPrice());
                TimeHelper.sleep(100, 200);
                return false;
            }

        } else {

            Store.setAction("Attempting to decrease price for " + pair.getName() + ".");
            GrandExchangeSetup.decreasePrice(pair.getIncreasePriceTimes());
        }

        TimeHelper.sleep(1500, 2500);

        log("Confirming offer.");
        GrandExchangeSetup.confirm();

        TimeHelper.sleep(500, 1000);

        return getOffer(pair) != null;
    }

    public RSGrandExchangeOffer getOffer(ItemPair pair) {
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers(s ->
                s.getType() == RSGrandExchangeOffer.Type.SELL &&
                        s.getItemDefinition() != null &&
                        s.getItemDefinition().getName().toLowerCase().equals(pair.getName().toLowerCase()));
        if (offers.length == 0) {
            return null;
        }
        return offers[0];
    }

    public boolean sell() {
        log("Executing Grand Exchange Seller");
        collect();
        int purchaseCount = 0;
        for (ItemPair pair : pairs) {
            if (!shouldSell(pair)) {
                log("Should not sell: " + pair.getName());
                purchaseCount++;
                continue;
            }
            if (!sell(pair)) {
                log("Sell is not completed yet for " + pair.getName());
                TimeHelper.sleep(1000, 1800);
                break;
            }
            TimeHelper.sleep(1500, 2500);
        }
        return purchaseCount == pairs.size();
    }

    private boolean shouldSell(ItemPair pair) {
        if (getOffer(pair) != null) {
            log("Offer already exists for" + pair.getName());
            return false;
        }
        boolean contains = Inventory.contains(pair.getName());
        if(!contains) {
            log("Inventory does not contain " + pair.getName() + ".");
        }
        return contains;
    }

    public void collect() {
        if (GrandExchange.getOffers(s -> s.getProgress() == RSGrandExchangeOffer.Progress.FINISHED).length == 0) {
            log("No offers finished, can not collect.");
            return;
        }
        if (!GrandExchange.isOpen()) {
            log("Grand Exchange not open, can not collect.");
            GrandExchangeHelper.open();
            return;
        }
        log("Collecting all offers.");
        GrandExchange.collectAll();
        TimeHelper.sleep(450, 850);
    }

    private void log(String message) {
        Logger.fine("GrandExchangeSeller", message);
    }

}
