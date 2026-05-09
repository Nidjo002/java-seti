package com.seti.model;

public enum PlanetType {
    EARTH("Earth", 1, 1),
    MERCURY("Mercury", 1, 3),
    VENUS("Venus", 1, 6),
    MARS("Mars", 2, 4),
    JUPITER("Jupiter", 3, 2),
    SATURN("Saturn", 3, 6),
    URANUS("Uranus", 4, 3),
    NEPTUNE("Neptune", 4, 7);

    private final String displayName;
    private final int ring;
    private final int sector;

    PlanetType(String displayName, int ring, int sector) {
        this.displayName = displayName;
        this.ring = ring;
        this.sector = sector;
    }

    public String getDisplayName() { return displayName; }
    public int getRing() { return ring; }
    public int getSector() { return sector; }
}