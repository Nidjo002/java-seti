package com.seti.model;

public enum StarType {
    SIRIUS("Sirius", 1),
    ALPHA_CENTAURI("Alpha Centauri", 2),
    VEGA("Vega", 3),
    ARCTURUS("Arcturus", 4),
    BETELGEUSE("Betelgeuse", 5),
    RIGEL("Rigel", 6),
    ALDEBARAN("Aldebaran", 7),
    POLARIS("Polaris", 8);

    private final String displayName;
    private final int sector;

    StarType(String displayName, int sector) {
        this.displayName = displayName;
        this.sector = sector;
    }

    public String getDisplayName() { return displayName; }
    public int getSector() { return sector; }
}