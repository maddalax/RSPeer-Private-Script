package org.maddev.helpers.time;

import org.rspeer.runetek.api.ClientSupplier;

/**
 * @author Jacob Rhiel - Intelli created Feb, 2019
 */
public class Animation {

    /**
     * Retrieves the length of the current animation in ticks
     * @param animationId The animation id
     * @return The amount of ticks the duration of the animation is
     */
    public static int getDurationTicks(int animationId) {
        return getDurationTicks(ClientSupplier.get().getAnimationSequence(animationId).getFrameLengths());
    }

    /**
     * Gets the MS time of an animation
     * @param lengths The frame lengths of the animation definition
     * @return The time in MS of the animation
     */
    public static int getDurationMS(int[] lengths) {
        if (lengths == null) {
            return 0;
        }
        int ms = 0;
        for (int i : lengths) {
            ms += i;
        }
        return ms * 30;
    }

    /**
     * Gets the amount of ticks an animations duration is
     * @param lengths The frame lengths of the animation definition
     * @return The amount of ticks the animations duration is
     */
    public static int getDurationTicks(int[] lengths) {
        int duration = getDurationMS(lengths) / 600;
        return duration < 1 ? 1 : duration;
    }

}