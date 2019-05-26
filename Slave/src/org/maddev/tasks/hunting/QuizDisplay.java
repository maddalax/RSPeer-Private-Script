package org.maddev.tasks.hunting;

import org.rspeer.runetek.api.movement.position.Position;

public enum QuizDisplay {
    LIZARD(new Position(1743, 4977, 0), "Lacertilia"),
    TORTOISE(new Position(1753, 4977, 0), "Testudines"),
    DRAGON(new Position(1768, 4977, 0), "Rex"),
    WYVERN(new Position(1778, 4977, 0), "Ossis"),
    SNAIL(new Position(1776, 4962, 0), "Achatina"),
    SNAKE(new Position(1783, 4962, 0), "Serpentes"),
    SEASLUG(new Position(1781, 4958, 0), "Opisthobranchia"),
    MONKEY(new Position(1774, 4958, 0), "Simiiformes"),
    KALPHITE(new Position(1761, 4938, 0), "Kalphiscarabeinae"),
    TERRORBIRD(new Position(1756, 4940, 0), "Aves"),
    PENGUIN(new Position(1742, 4958, 0), "Spheniscidae"),
    MOLE(new Position(1735, 4958, 0), "Talpidae"),
    CAMEL(new Position(1737, 4962, 0), "Camelus"),
    LEECH(new Position(1744, 4962, 0), "Hirudinea");

    private final Position display;
    private final String chatMsg;

    QuizDisplay(Position display, String chatMsg) {
        this.display = display;
        this.chatMsg = chatMsg;
    }

    public Position getDisplay() {
        return display;
    }

    public String getChatMsg() {
        return chatMsg;
    }
}

