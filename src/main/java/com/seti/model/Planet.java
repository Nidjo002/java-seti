package com.seti.model;

import java.io.Serializable;
import java.io.Serial;

public class Planet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final PlanetType type;
    private boolean firstOrbited;
    private boolean firstLanded;

    public Planet(PlanetType type) {
        this.type = type;
        this.firstOrbited = false;
        this.firstLanded = false;
    }

    public boolean claimFirstOrbit() {
        if (firstOrbited) return false;
        firstOrbited = true;
        return true;
    }

    public boolean claimFirstLanding() {
        if (firstLanded) return false;
        firstLanded = true;
        return true;
    }

    public String getName() { return type.getDisplayName(); }
    public PlanetType getType() { return type; }
    public boolean isFirstOrbited() { return firstOrbited; }
    public boolean isFirstLanded() { return firstLanded; }
}