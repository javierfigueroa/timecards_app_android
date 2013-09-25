package com.timecards.app;

import com.timecards.api.model.Timecard;

/**
 * Created by javier on 9/24/13.
 */
public class CurrentTimecard {

    private static Timecard timecard;

    public static Timecard getCurrentTimecard() {
        if (timecard == null) {
            timecard = new Timecard();
        }

        return timecard;
    }

    public static void setCurrentTimecard(Timecard timecard) {
        CurrentTimecard.timecard = timecard;
    }
}
