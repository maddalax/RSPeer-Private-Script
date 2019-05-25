package org.maddev.helpers.crafting;

import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;

public class CraftingHelper {

    public static final double LEATHER_GLOVES_XP = 13.8;
    public static final double LEATHER_BOOTS_XP = 16.25;
    public static final double FLAX_XP = 15;

    public static final int SPOOL_PER_ITEM = 5;
    public static final int REQUIRED_LEVEL = 31;

    public static int getQuantityNeeded(int level, double xp) {
        double current = Skills.getExperience(Skill.CRAFTING);
        double desired = Skills.getExperienceAt(level);
        return (int) ((desired - current) / xp) + 1;
    }
}
