package com.seti.ui;

import com.seti.model.GameConfig;

public final class BoardGeometry {

    public static final double RING_GAP = 82.0;
    public static final double SUN_RADIUS = 40.0;
    public static final double CELL_RADIUS = 28.0;
    public static final double STAR_RING_OFFSET = 72.0;

    private BoardGeometry() {}

    public static double[] getCellPosition(int ring, int sector, double cx, double cy) {
        double radius = SUN_RADIUS + ring * RING_GAP;
        double angle = getSectorMidAngle(sector);
        return new double[]{cx + radius * Math.cos(angle), cy + radius * Math.sin(angle)};
    }

    public static double getSectorMidAngle(int sector) {
        double sectorAngle = (sector - 1) * 2 * Math.PI / GameConfig.NUM_SECTORS;
        return sectorAngle + Math.PI / GameConfig.NUM_SECTORS;
    }

    public static double getStarRingRadius(double cx, double cy) {
        return SUN_RADIUS + GameConfig.NUM_RINGS * RING_GAP + STAR_RING_OFFSET;
    }
}