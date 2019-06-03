package org.maddev.web.dax;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.rspeer.runetek.api.scene.Players;

import java.util.ArrayList;
import java.util.List;

@DoNotRename
public class PlayerDetails {

    @DoNotRename
    private int floor;

    @DoNotRename
    private int attack;

    @DoNotRename
    private int defence;

    @DoNotRename
    private int strength;

    @DoNotRename
    private int hitpoints;

    @DoNotRename
    private int ranged;

    @DoNotRename
    private int prayer;

    @DoNotRename
    private int magic;

    @DoNotRename
    private int cooking;

    @DoNotRename
    private int woodcutting;

    @DoNotRename
    private int fletching;

    @DoNotRename
    private int fishing;

    @DoNotRename
    private int firemaking;

    @DoNotRename
    private int crafting;

    @DoNotRename
    private int smithing;

    @DoNotRename
    private int mining;

    @DoNotRename
    private int herblore;

    @DoNotRename
    private int agility;

    @DoNotRename
    private int thieving;

    @DoNotRename
    private int slayer;

    @DoNotRename
    private int farming;

    @DoNotRename
    private int runecrafting;

    @DoNotRename
    private int hunter;

    @DoNotRename
    private int construction;

    @DoNotRename
    private List<IntPair> setting;

    @DoNotRename
    private List<IntPair> varbit;

    @DoNotRename
    private boolean member;

    @DoNotRename
    private List<IntPair> equipment;

    @DoNotRename
    private List<IntPair> inventory;

    public PlayerDetails() {
        setting = new ArrayList<>();
        varbit = new ArrayList<>();
        equipment = new ArrayList<>();
        inventory = new ArrayList<>();
        floor = Players.getLocal().getPosition().getFloorLevel();
    }

    public int getAttack() {
        return attack;
    }

    public int getDefence() {
        return defence;
    }

    public int getStrength() {
        return strength;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public int getRanged() {
        return ranged;
    }

    public int getPrayer() {
        return prayer;
    }

    public int getMagic() {
        return magic;
    }

    public int getCooking() {
        return cooking;
    }

    public int getWoodcutting() {
        return woodcutting;
    }

    public int getFletching() {
        return fletching;
    }

    public int getFishing() {
        return fishing;
    }

    public int getFiremaking() {
        return firemaking;
    }

    public int getCrafting() {
        return crafting;
    }

    public int getSmithing() {
        return smithing;
    }

    public int getMining() {
        return mining;
    }

    public int getHerblore() {
        return herblore;
    }

    public int getAgility() {
        return agility;
    }

    public int getThieving() {
        return thieving;
    }

    public int getSlayer() {
        return slayer;
    }

    public int getFarming() {
        return farming;
    }

    public int getRunecrafting() {
        return runecrafting;
    }

    public int getHunter() {
        return hunter;
    }

    public int getConstruction() {
        return construction;
    }

    public List<IntPair> getSetting() {
        return setting;
    }

    public List<IntPair> getVarbit() {
        return varbit;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isMember() {
        return member;
    }

    public List<IntPair> getEquipment() {
        return equipment;
    }

    public List<IntPair> getInventory() {
        return inventory;
    }

    public JsonElement toJson() {
        return new Gson().toJsonTree(this);
    }

}