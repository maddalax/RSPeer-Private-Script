package org.maddev.ws.data;

import com.allatori.annotations.DoNotRename;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DoNotRename
public class ClientInfo {

    @DoNotRename
    private String email;
    @DoNotRename
    private int userId;
    @DoNotRename
    private String ipAddress;
    @DoNotRename
    private String proxyIp;
    @DoNotRename
    private String proxyUsername;
    @DoNotRename
    private String scriptName;
    @DoNotRename
    private String rsn;
    @DoNotRename
    private String runescapeLogin;
    @DoNotRename
    private String machineName;
    @DoNotRename
    private String javaVersion;
    @DoNotRename
    private String operatingSystem;
    @DoNotRename
    private String machineUserName;
    @DoNotRename
    public String scriptClassName;
    @DoNotRename
    public String scriptDeveloper;
    @DoNotRename
    public double version;
    @DoNotRename
    public Map<String, Integer> bank;
    @DoNotRename
    public Map<String, Integer> inventory;
    @DoNotRename
    public Map<String, Integer> equipment;
    @DoNotRename
    public long lastUpdate;

    @DoNotRename
    public String task;

    @DoNotRename
    public String action;

    @DoNotRename
    public Map<String, Integer> skills;

    public String getRunescapeLogin() {
        return runescapeLogin;
    }

    public void setRunescapeLogin(String runescapeLogin) {
        this.runescapeLogin = runescapeLogin;
    }

    public String getScriptClassName() {
        return scriptClassName;
    }

    public void setScriptClassName(String scriptClassName) {
        this.scriptClassName = scriptClassName;
    }

    public String getScriptDeveloper() {
        return scriptDeveloper;
    }

    public void setScriptDeveloper(String scriptDeveloper) {
        this.scriptDeveloper = scriptDeveloper;
    }

    public String getEmail() {
        return email;
    }

    public int getUserId() {
        return userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getRsn() {
        return rsn;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = proxyIp;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public void setRsn(String rsn) {
        this.rsn = rsn;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getMachineUserName() {
        return machineUserName;
    }

    public void setMachineUserName(String machineUserName) {
        this.machineUserName = machineUserName;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public double getVersion() {
        return version;
    }

    public Map<String, Integer> getBank() {
        return bank;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public Map<String, Integer> getEquipment() {
        return equipment;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public String getTask() {
        return task;
    }

    public void setBank(Map<String, Integer> bank) {
        this.bank = bank;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    public void setEquipment(Map<String, Integer> equipment) {
        this.equipment = equipment;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setSkills(Map<String, Integer> skills) {
        this.skills = skills;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
