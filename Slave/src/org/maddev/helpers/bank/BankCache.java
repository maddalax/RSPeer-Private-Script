package org.maddev.helpers.bank;

import org.maddev.State;
import org.maddev.Store;
import org.rspeer.runetek.adapter.component.Item;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.component.Bank;
import org.maddev.helpers.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BankCache {

    private static Map<String, Integer> cache = new HashMap<>();
    private static ScheduledExecutorService executor;
    public static long lastUpdate;

    static {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(BankCache::cache, 1, 15, TimeUnit.SECONDS);
    }

    public static void dispose() {
        executor.shutdown();
    }

    public static boolean contains(String name) {
        initialize();
        return cache.containsKey(name) || Bank.contains(name);
    }

    public static int getCount(String name) {
        initialize();
        if(Bank.isOpen()) {
            return Bank.getCount(name);
        }
        return cache.getOrDefault(name, 0);
    }

    public static boolean isCached() {
       return cache.size() != 0;
    }

    private static void initialize() {
        while (!isCached()) {
            Logger.fine("Caching bank.");
            Store.setAction("Caching bank.");
            if(Store.getState() == State.SCRIPT_STOPPED) {
                break;
            }
            if(Bank.isOpen()) {
                int length = Bank.getItems().length;
                Logger.fine("Bank items size: " + length);
                if(length > 0) {
                    cache();
                    break;
                }
                TimeHelper.sleep(350, 650);
                continue;
            }
            BankHelper.open(BankHelper.nearest(), true);
        }
    }

    public static Map<String, Integer> getCache() {
        return cache;
    }

    public static void cache() {
        if (Bank.isOpen()) {
            Map<String, Integer> bank = new HashMap<>();
            for (Item item : Bank.getItems()) {
                bank.put(item.getName(), item.getStackSize() + bank.getOrDefault(item.getName(), 0));
            }
            lastUpdate = System.currentTimeMillis();
            cache = bank;
        }
    }

    public static int getLastUpdate() {
        return (int) ((System.currentTimeMillis() - lastUpdate) / 1000);
    }
}
