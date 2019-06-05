package org.maddev.helpers.grand_exchange;

import org.maddev.Store;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;

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

    public boolean hasItem(String name) {
        for (ItemPair pair : pairs) {
            if (pair.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean sell(ItemPair pair) {
        if (!GrandExchangeHelper.open()) {
            return false;
        }

        if (!GrandExchangeSetup.isOpen()) {
            Logger.fine("Attempting to create offer.");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL);
            Time.sleep(500, 1000);
            return false;
        }

        Store.setAction("Creating offer for " + pair.getName());

        if (GrandExchangeSetup.getItem() == null) {
            Logger.fine("Attempting to create offer.");
            GrandExchangeSetup.setItem(pair.getName());
            return false;
        }

        if (pair.getOriginalPrice() == 0) {
            pair.setOriginalPrice(GrandExchangeSetup.getPricePerItem());
        }

        if (pair.getPrice() != 0) {

            if (GrandExchangeSetup.getPricePerItem() != pair.getPrice()) {
                Store.setAction("Setting price to " + pair.getPrice() + ".");
                GrandExchangeSetup.setPrice(pair.getPrice());
                Time.sleep(100, 200);
                return false;
            }

        } else {

            Store.setAction("Attempting to decrease price.");
            GrandExchangeSetup.decreasePrice(pair.getIncreasePriceTimes());
        }

        Time.sleep(1500, 2500);

        GrandExchangeSetup.confirm();

        Time.sleep(500, 1000);

        return getOffer(pair) != null;
    }

    private RSGrandExchangeOffer getOffer(ItemPair pair) {
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
        collect();
        int purchaseCount = 0;
        for (ItemPair pair : pairs) {
            if (!shouldSell(pair)) {
                purchaseCount++;
                continue;
            }
            if (!sell(pair)) {
                Time.sleep(1000, 1800);
                break;
            } else {
                pairs.remove(pair);
            }
            Time.sleep(1500, 2500);
        }
        return purchaseCount == pairs.size();
    }

    private boolean shouldSell(ItemPair pair) {
        if (getOffer(pair) != null) {
            return false;
        }
        return Inventory.contains(pair.getName());
    }

    private boolean collect() {
        if (GrandExchange.getOffers(s -> s.getProgress() == RSGrandExchangeOffer.Progress.FINISHED).length == 0) {
            return false;
        }
        if (!GrandExchange.isOpen()) {
            GrandExchangeHelper.open();
            return false;
        }
        return GrandExchange.collectAll();
    }

}
