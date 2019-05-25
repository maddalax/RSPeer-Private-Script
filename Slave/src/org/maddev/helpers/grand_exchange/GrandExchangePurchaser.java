package org.maddev.helpers.grand_exchange;

import org.maddev.helpers.bank.BankCache;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;

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
        System.out.println("Adding: " + pair.getName());
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

        System.out.println("Creating offer for " + pair.getName());

        if (!GrandExchangeSetup.isOpen()) {
            System.out.println("Attempting to create offer.");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
            Time.sleep(500, 1000);
            return false;
        }

        if (GrandExchangeSetup.getItem() == null) {
            System.out.println("Attempting to set item");
            GrandExchangeSetup.setItem(pair.getName());
            Time.sleep(500, 1000);
            return false;
        }

        if (GrandExchangeSetup.getQuantity() != pair.getQuantity()) {
            System.out.println("Attempting to set quantity.");
            GrandExchangeSetup.setQuantity(pair.getQuantity());
            Time.sleep(500, 1000);
            return false;
        }

        if (pair.getOriginalPrice() == 0) {
            pair.setOriginalPrice(GrandExchangeSetup.getPricePerItem());
        }

        if(pair.getPriceMinimum() != 0 && GrandExchangeSetup.getPricePerItem() < pair.getPriceMinimum()) {
            System.out.println("Attempting to set price.");
            GrandExchangeSetup.setPrice(pair.getPriceMinimum());
        } else {
            System.out.println("Attempting to increase price.");
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
            System.out.println("Attempting to purchase: " + pair.getName() + " for " + pair.getQuantity());
            if (!purchase(pair)) {
                break;
            } else {
                pairs.remove(pair);
            }
            Time.sleep(1000, 1500);
        }
        return purchaseCount == pairs.size();
    }

    private boolean shouldBuy(ItemPair pair) {
        if (getOffer(pair) != null) {
            return false;
        }
        if (Inventory.contains(pair.getName())) {
            return false;
        }
        if (BankCache.contains(pair.getName())) {
            return false;
        }
        if(Equipment.contains(pair.getName())) {
            return false;
        }
        return true;
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
        System.out.println("Attempting to open Grand Exchange");
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
