package org.maddev.helpers.mule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class DateHelper {

    public static ZonedDateTime getUtc() {
        ZonedDateTime time = Instant.now().atZone(ZoneId.of("UTC"));
        long millis = TimeUnit.HOURS.toMillis(time.getHour());
        millis += TimeUnit.MINUTES.toMillis(time.getMinute());
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC"));
    }

    public static ZonedDateTime getUtc(String value) {
        String[] split = value.split(":");
        int hour = Integer.parseInt(split[0]);
        int minute = Integer.parseInt(split[1]);
        long millis = TimeUnit.HOURS.toMillis(hour);
        millis += TimeUnit.MINUTES.toMillis(minute);
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC"));
    }

}
