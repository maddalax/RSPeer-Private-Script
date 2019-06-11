package org.maddev.helpers.dialogue;

import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.rspeer.runetek.adapter.scene.Npc;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.scene.Npcs;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.log.Logger;

public class DialogueHelper {

    public static void process(String name, String... options) {
        if(!Dialog.isOpen()) {
            Npc npc = Npcs.getNearest(name);
            if(npc == null) {
                Logger.severe("Attempting to process dialogue " + String.join(" , ", options) + " but NPC by name: " + name + " cannot be found.");
                return;
            }
            processWithEntity(npc, options);
            return;
        }
        processWithEntity(null, options);
    }

    public static void processWithEntity(Npc npc, String... options) {
        if(!Dialog.isOpen()) {
            if(npc == null) {
                Logger.severe("Attempting to process dialogue " + String.join(" , ", options) + " but NPC is null.");
                return;
            }
            InteractHelper.interact(npc, "Talk-to");
            TimeHelper.sleepUntil(Dialog::isOpen, 2500);
            return;
        }
        if (Dialog.canContinue()) {
            Logger.fine("Processing continue.");
            Dialog.processContinue();
            TimeHelper.sleep(350, 650);
            return;
        }
        if (Dialog.isProcessing()) {
            Logger.fine("Dialogue is processing.");
            TimeHelper.sleep(100, 500);
            return;
        }
        Store.setAction("Processing Dialogue.");
        Logger.fine("Looking for Dialogue: " + String.join(" , ", options));
        Dialog.process(options);
        TimeHelper.sleep(350, 650);
    }

}
