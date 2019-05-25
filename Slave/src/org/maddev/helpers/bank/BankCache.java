package org.maddev.helpers.bank;

import org.maddev.State;
import org.maddev.Store;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;

import java.util.HashMap;
import java.util.Map;

public class BankCache {

    private static Map<String, Integer> cache = new HashMap<>();

    public static boolean contains(String name) {
        while (!isCached()) {
            if(Store.getState() == State.SCRIPT_STOPPED) {
                break;
            }
            if(Bank.isOpen()) {
                if(Bank.getItems().length > 0) {
                    cache();
                    break;
                }
                Time.sleep(350, 650);
                continue;
            }
            System.out.println("Opening bank to get cache.");
            BankHelper.open();
        }
        return cache.containsKey(name) || Bank.contains(name);
    }

    private static boolean isCached() {
       return cache.size() != 0;
    }

    public static void cache() {
        if (Bank.isOpen()) {
            Map<String, Integer> bank = new HashMap<>();
            for (Item item : Bank.getItems()) {
                bank.put(item.getName(), item.getStackSize() + bank.getOrDefault(item.getName(), 0));
            }
            cache = bank;
        }
    }

}
