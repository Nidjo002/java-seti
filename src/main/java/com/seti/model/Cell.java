package com.seti.model;

import java.io.Serializable;

public record Cell(int ring, int sector, Planet planet)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public Cell(int ring, int sector) {
        this(ring, sector, null);
    }

    public boolean hasPlanet() {
        return planet != null;
    }

    private boolean isLandablePlanet() {
        return planet != null && planet.getType() != PlanetType.EARTH;
    }

    public boolean canOrbit() { return isLandablePlanet(); }
    public boolean canLand() { return isLandablePlanet(); }
}