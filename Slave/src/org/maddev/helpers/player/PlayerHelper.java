package org.maddev.helpers.player;

import org.maddev.helpers.bank.BankCache;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;

public class PlayerHelper {

    public static boolean hasAll(String ... names) {
        for (String name : names) {
            if(!BankCache.contains(name) && !Inventory.contains(name) && !Equipment.contains(name)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAny(String ... names) {
        for (String name : names) {
            if(BankCache.contains(name) || Inventory.contains(name) || Equipment.contains(name)) {
                return true;
            }
        }
        return false;
    }

}

