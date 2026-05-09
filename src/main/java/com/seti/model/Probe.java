package com.seti.model;

import java.io.Serializable;

public class Probe implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProbeStatus status;
    private int currentRing;
    private int currentSector;

    public Probe() {
        this.status = ProbeStatus.READY;
        this.currentRing = 0;
        this.currentSector = 0;
    }

    public void launchProbe() {
        this.currentRing = 1;
        this.currentSector = 1;
        this.status = ProbeStatus.IN_FLIGHT;
    }

    public void moveTo(int ring, int sector) {
        this.currentRing = ring;
        this.currentSector = sector;
    }

    public void resetProbe() {
        this.status = ProbeStatus.READY;
        this.currentRing = 0;
        this.currentSector = 0;
    }

    public boolean isProbeOnMap() {
        return status == ProbeStatus.IN_FLIGHT;
    }

    public ProbeStatus getProbeStatus() { return status; }
    public int getProbeCurrentRing() { return currentRing; }
    public int getProbeCurrentSector() { return currentSector; }
}