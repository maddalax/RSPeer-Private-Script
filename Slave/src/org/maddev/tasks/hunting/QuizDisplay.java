package org.maddev.tasks.hunting;

import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.movement.position.Position;

public enum QuizDisplay {
    LIZARD(2048, new Position(1743, 4977, 0), "Lacertilia"),
    TORTOISE(2050, new Position(1753, 4977, 0), "Testudines"),
    DRAGON(2052, new Position(1768, 4977, 0), "Rex"),
    WYVERN(2054, new Position(1778, 4977, 0), "Ossis"),
    SNAIL(2056, new Position(1776, 4962, 0), "Achatina"),
    SNAKE(2058, new Position(1783, 4962, 0), "Serpentes"),
    SEASLUG(2060, new Position(1781, 4958, 0), "Opisthobranchia"),
    MONKEY(2062, new Position(1774, 4958, 0), "Simiiformes"),
    KALPHITE(2064, new Position(1761, 4938, 0), "Kalphiscarabeinae"),
    TERRORBIRD(2066, new Position(1756, 4940, 0), "Aves"),
    PENGUIN(2068, new Position(1742, 4958, 0), "Spheniscidae"),
    MOLE(2070, new Position(1735, 4958, 0), "Talpidae"),
    CAMEL(2072, new Position(1737, 4962, 0), "Camelus"),
    LEECH(2074, new Position(1744, 4962, 0), "Hirudinea");

    private final Position display;
    private final String chatMsg;
    private final int varp;

    QuizDisplay(int varp, Position display, String chatMsg) {
        this.varp = varp;
        this.display = display;
        this.chatMsg = chatMsg;
    }

    public static QuizDisplay getCurrent() {
        int value = Varps.get(1010);
        for (QuizDisplay quiz : values()) {
            if(quiz.varp == value) {
                return quiz;
            }
        }
        return null;
    }

    public Position getDisplay() {
        return display;
    }

    public String getChatMsg() {
        return chatMsg;
    }
}

