package org.maddev.ws.data;

import org.maddev.Main;
import org.maddev.Store;
import org.maddev.helpers.bank.BankCache;
import org.maddev.helpers.http.HttpHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.scene.Players;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ClientInfoAggregator {

    private static String ipAddress;

    public static ClientInfo execute() {
        ClientInfo info = new ClientInfo();
        info.setMachineName(getComputerName());
        info.setJavaVersion(String.valueOf(getJavaVersion()));
        info.setProxyIp(System.getProperty("socksProxyHost"));
        info.setProxyUsername(System.getProperty("java.net.socks.username"));
        info.setOperatingSystem(System.getProperty("os.name"));
        info.setMachineUserName(System.getProperty("user.name"));
        if (Game.getClient() != null) {
            String name = Game.getClient().getUsername();
            info.setRunescapeLogin(name);
            if (name != null) {
                Player player = Players.getLocal();
                if (player != null) {
                    info.setRsn(player.getName());
                }
            }
        }
        if (ipAddress == null) {
            ipAddress = HttpHelper.getIpAddress();
        }
        info.setIpAddress(ipAddress);

        info.setBank(BankCache.getCache());
        info.setInventory(getInventory());
        info.setEquipment(getEquipment());
        info.setSkills(getSkills());
        info.setLastUpdate(System.currentTimeMillis());
        info.setTask(Store.getTask());
        info.setAction(Store.getAction());
        return info;
    }

    private static Map<String, Integer> getInventory() {
        if(Game.getClient() == null || !Game.isLoggedIn()) {
            return new HashMap<>();
        }
        Map<String, Integer> dict = new HashMap<>();
        for (Item item : Inventory.getItems()) {
            dict.putIfAbsent(item.getName(), Inventory.getCount(true, item.getName()));
        }
        return dict;
    }

    private static Map<String, Integer> getEquipment() {
        if(Game.getClient() == null || !Game.isLoggedIn()) {
            return new HashMap<>();
        }
        Map<String, Integer> dict = new HashMap<>();
        for (Item item : Equipment.getItems()) {
            dict.putIfAbsent(item.getName(), Equipment.getCount(true, item.getName()));
        }
        return dict;
    }

    private static Map<String, Integer> getSkills() {
        if(Game.getClient() == null || !Game.isLoggedIn()) {
            return new HashMap<>();
        }
        Map<String, Integer> dict = new HashMap<>();
        for (Skill item : Skill.values()) {
            dict.putIfAbsent(item.name(), Skills.getCurrentLevel(item));
        }
        return dict;
    }


    public static Callable<String> executeToString() {
        return () -> Main.gson.toJson(execute());
    }

    private static String getComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String host = System.getenv("COMPUTERNAME");
        if (host != null) {
            return host;
        }
        host = System.getenv("HOSTNAME");
        if (host != null) {
            return host;
        }
        return null;
    }

    private static double getJavaVersion() {
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos + 1);
        return Double.parseDouble(version.substring(0, pos));
    }
}
