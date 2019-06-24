package org.maddev.helpers.mule;

import org.maddev.Config;

public class MuleHelper {

    public static boolean isFullMuleTime() {
        String timeWhen = Config.MULE_WHEN_TIME;
        if(timeWhen == null) {
            return false;
        }
        return DateHelper.getUtc().isAfter(DateHelper.getUtc(timeWhen));
    }

}
