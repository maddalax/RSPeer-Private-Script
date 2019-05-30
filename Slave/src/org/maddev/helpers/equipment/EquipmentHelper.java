package org.maddev.helpers.equipment;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;

import java.util.Arrays;
import java.util.List;

public class EquipmentHelper {

    public static String[] getChargedGlories() {
        String[] names = new String[6];
        for(int i = 0; i < 6; i++) {
            String name = "Amulet of glory(" + (i + 1) + ")";
            names[i] = name;
        }
        return names;
    }

    public static String[] getGamesNecklaces() {
        String[] names = new String[8];
        for(int i = 0; i < 8; i++) {
            String name = "Games necklace(" + (i + 1) + ")";
            names[i] = name;
        }
        return names;
    }

    public static Item getChargedGlory() {
        List<String> glories = Arrays.asList(EquipmentHelper.getChargedGlories());
        Item[] equipmentItems = Equipment.getItems(i -> glories.contains(i.getName()));
        if(equipmentItems.length > 0) {
            System.out.println("Is from equipment");
            return equipmentItems[0];
        }
        return Inventory.getFirst(i -> glories.contains(i.getName()));
    }

    public static Item getChargedGamesNecklace() {
        List<String> games = Arrays.asList(EquipmentHelper.getGamesNecklaces());
        Item[] equipmentItems = Equipment.getItems(i -> games.contains(i.getName()));
        if(equipmentItems.length > 0) {
            return equipmentItems[0];
        }
        return Inventory.getFirst(i -> games.contains(i.getName()));
    }

}
