package org.maddev.tasks;

import org.maddev.helpers.bank.BankHelper;
import org.maddev.helpers.equipment.EquipmentHelper;
import org.maddev.helpers.player.PlayerHelper;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

public class EquipGlory extends Task {

    @Override
    public boolean validate() {
        if(EquipmentSlot.NECK.getItem() != null) {
            return false;
        }
        if(PlayerHelper.hasAny(EquipmentHelper.getChargedGlories())) {
            Item glory = EquipmentHelper.getChargedGlory();
            return glory == null || !Equipment.contains(glory.getName());
        }
        return false;
    }

    @Override
    public int execute() {
        Log.fine("Equipping amulet glory.");
        Item exists = EquipmentHelper.getChargedGlory();
        if(exists == null) {
            for (String glory : EquipmentHelper.getChargedGlories()) {
                BankHelper.withdraw(glory, 1);
                Time.sleep(850, 1550);
            }
        }
        else {
            exists.interact("Wear");
            Time.sleep(800, 1100);
        }
        return Random.nextInt(350, 850);
    }
}
