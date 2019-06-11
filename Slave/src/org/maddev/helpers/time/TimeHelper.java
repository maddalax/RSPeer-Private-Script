package org.maddev.helpers.time;

import org.maddev.helpers.log.Logger;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;

import java.util.function.BooleanSupplier;

public class TimeHelper {

    public static void sleep(int min, int max) {
        int length = Random.nextInt(min, max);
        Time.sleep(length);
    }

    public static void sleepUntil(BooleanSupplier supplier, int timeout) {
        Time.sleepUntil(supplier, timeout);
    }

    private static void log(String message) {
        Logger.fine("Time", message);
    }
}
