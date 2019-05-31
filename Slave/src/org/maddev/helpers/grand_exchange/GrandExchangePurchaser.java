package org.maddev.helpers.grand_exchange;

import org.maddev.helpers.player.PlayerHelper;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class GrandExchangePurchaser {

    private CopyOnWriteArrayList<ItemPair> pairs;

    public GrandExchangePurchaser(ItemPair... pairs) {
        this.pairs = new CopyOnWriteArrayList<>(pairs);
    }

    public void addItem(ItemPair pair) {
        if(hasItem(pair.getName())) {
            return;
        }
        pairs.add(pair);
    }

    public void clear() {
        pairs.clear();
    }

    public boolean hasItem(String name) {
        for (ItemPair pair : pairs) {
            if(pair.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean purchase(ItemPair pair) {
        if (!open()) {
            return false;
        }

        Log.fine("Creating offer for " + pair.getName());

        if (!GrandExchangeSetup.isOpen()) {
            Log.fine("Attempting to create offer.");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
            Time.sleep(500, 1000);
            return false;
        }

        if (GrandExchangeSetup.getItem() == null) {
            Log.fine("Attempting to set item");
            GrandExchangeSetup.setItem(pair.getName());
            Time.sleep(500, 1000);
            return false;
        }

        if(!GrandExchangeSetup.getItem().getName().equals(pair.getName())) {
            Log.fine("Attempting to set item");
            GrandExchangeSetup.setItem(pair.getName());
            Time.sleep(500, 1000);
            return false;
        }

        int current = PlayerHelper.getTotalCount(pair.getName());
        int quantity = pair.getQuantity() - current;
        if (GrandExchangeSetup.getQuantity() != quantity) {
            Log.fine("Attempting to set quantity to " + quantity + ".");
            GrandExchangeSetup.setQuantity(quantity);
            Time.sleep(500, 1000);
            return false;
        }

        if (pair.getOriginalPrice() == 0) {
            pair.setOriginalPrice(GrandExchangeSetup.getPricePerItem());
        }

        if(pair.getPriceMinimum() != 0 && GrandExchangeSetup.getPricePerItem() < pair.getPriceMinimum()) {
            Log.fine("Attempting to set price.");
            GrandExchangeSetup.setPrice(pair.getPriceMinimum());
        } else {
            Log.fine("Attempting to increase price.");
            GrandExchangeSetup.increasePrice(pair.getIncreasePriceTimes());
        }

        Time.sleep(1500, 2500);

        GrandExchangeSetup.confirm();

        Time.sleep(500, 1000);

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
        collect();
        int purchaseCount = 0;
        for (ItemPair pair : pairs) {
            if (!shouldBuy(pair)) {
                purchaseCount++;
                continue;
            }
            Log.fine("Attempting to purchase: " + pair.getName() + " for " + pair.getQuantity());
            if (!purchase(pair)) {
                Time.sleep(1000, 1800);
                break;
            } else {
                pairs.remove(pair);
            }
            Time.sleep(1500, 2500);
        }
        return purchaseCount == pairs.size();
    }

    private boolean shouldBuy(ItemPair pair) {
        if (getOffer(pair) != null) {
            return false;
        }
        int count = PlayerHelper.getTotalCount(pair.getName());
        Log.fine("Count " + count + " " + pair.getName());
        return count < pair.getQuantity();
    }

    private boolean collect() {
        if(GrandExchange.getOffers(s -> s.getProgress() == RSGrandExchangeOffer.Progress.FINISHED).length == 0) {
            return false;
        }
        if(!GrandExchange.isOpen()) {
            open();
            return false;
        }
        return GrandExchange.collectAll();
    }

    private static boolean open() {
        if (GrandExchange.isOpen()) {
            return true;
        }
        Log.fine("Attempting to open Grand Exchange");
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
