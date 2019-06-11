package org.maddev.helpers.grand_exchange;

import org.maddev.Store;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.player.PlayerHelper;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;

import java.util.concurrent.CopyOnWriteArrayList;

public class GrandExchangePurchaser {

    private CopyOnWriteArrayList<ItemPair> pairs;

    public GrandExchangePurchaser(ItemPair... pairs) {
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

    private boolean purchase(ItemPair pair) {

        if (!GrandExchangeHelper.open()) {
            Store.setAction("Opening Grand Exchange.");
            return false;
        }

        Store.setAction("Creating offer for " + pair.getName());

        if (!GrandExchangeSetup.isOpen()) {
            log("Attempting to create offer for " + pair.getName());
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
            TimeHelper.sleep(500, 1000);
            return false;
        }

        if (GrandExchangeSetup.getItem() == null) {
            Store.setAction("Attempting to set item -> " + pair.getName());
            GrandExchangeSetup.setItem(pair.getName());
            TimeHelper.sleep(500, 1000);
            return false;
        }

        if (!GrandExchangeSetup.getItem().getName().equals(pair.getName())) {
            Store.setAction("Attempting to set item -> " + pair.getName());
            GrandExchangeSetup.setItem(pair.getName());
            TimeHelper.sleep(500, 1000);
            return false;
        }

        int current = PlayerHelper.getTotalCount(pair.getName());
        int quantity = pair.getQuantity() - current;
        if (GrandExchangeSetup.getQuantity() != quantity) {
            Store.setAction("Setting quantity to " + quantity + ".");
            GrandExchangeSetup.setQuantity(quantity);
            TimeHelper.sleep(500, 1000);
            return false;
        }

        if (pair.getOriginalPrice() == 0) {
            pair.setOriginalPrice(GrandExchangeSetup.getPricePerItem());
        }

        if (pair.getPrice() != 0) {

            if (GrandExchangeSetup.getPricePerItem() != pair.getPrice()) {
                Store.setAction("Setting price to " + pair.getPrice() + ".");
                GrandExchangeSetup.setPrice(pair.getPrice());
                TimeHelper.sleep(100, 200);
                return false;
            }

        } else {

            if (pair.getPriceMinimum() != 0 && GrandExchangeSetup.getPricePerItem() < pair.getPriceMinimum()) {
                Store.setAction("Setting price.");
                GrandExchangeSetup.setPrice(pair.getPriceMinimum());
                TimeHelper.sleep(100, 200);
                return false;
            } else {
                Store.setAction("Attempting to increase price.");
                GrandExchangeSetup.increasePrice(pair.getIncreasePriceTimes());
            }
        }

        TimeHelper.sleep(1500, 2500);

        log("Confirming offer.");
        GrandExchangeSetup.confirm();

        TimeHelper.sleep(500, 1000);

        return getOffer(pair) != null;
    }

    private RSGrandExchangeOffer getOffer(ItemPair pair) {
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers(s ->
                s.getType() == RSGrandExchangeOffer.Type.BUY &&
                        s.getItemDefinition() != null &&
                        s.getItemDefinition().getName().toLowerCase().equals(pair.getName().toLowerCase()));
        if (offers.length == 0) {
            return null;
        }
        return offers[0];
    }

    public boolean purchase() {
        log("Executing Grand Exchange Buyer");
        collect();
        int purchaseCount = 0;
        for (ItemPair pair : pairs) {
            if (!shouldBuy(pair)) {
                log("Should not buy: " + pair.getName());
                purchaseCount++;
                continue;
            }
            if (!purchase(pair)) {
                log("Purchase is not completed yet for " + pair.getName());
                TimeHelper.sleep(1000, 1800);
                break;
            }
            TimeHelper.sleep(1500, 2500);
        }
        return purchaseCount == pairs.size();
    }

    private boolean shouldBuy(ItemPair pair) {
        if (getOffer(pair) != null) {
            log("Offer exists for " + pair.getName() + ".");
            return false;
        }
        int count = PlayerHelper.getTotalCount(pair.getName());
        boolean quantity = count < pair.getQuantity();
        log("Count for " + pair.getName() + " is less than specified quantity: " + quantity);
        return quantity;
    }

    private void collect() {
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
        Logger.fine("Grand Exchange Buyer: " + message);
    }

}
