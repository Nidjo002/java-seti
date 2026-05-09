package com.seti.model;

public enum TrailType {
    PINK("Pink", 10),
    YELLOW("Yellow", 7),
    BLUE("Blue", 15);

    private final String displayName;
    private final int victoryPoints;

    TrailType(String displayName, int victoryPoints) {
        this.displayName = displayName;
        this.victoryPoints = victoryPoints;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }
}