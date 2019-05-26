package org.maddev.helpers.equipment;

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

}
