package com.seti.model;

public final class GameConfig {

    public static final int NUM_ROUNDS = 5;
    public static final int NUM_RINGS = 4;
    public static final int NUM_SECTORS = 8;
    public static final int MAX_PLAYERS = 2;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_SCANS_PER_SECTOR = 4;

    public static final int STARTING_CREDITS = 5;
    public static final int STARTING_ENERGY = 5;
    public static final int STARTING_DATA_TOKENS = 0;

    public static final int INCOME_CREDITS = 3;
    public static final int INCOME_ENERGY = 4;

    public static final int VP_ORBIT_FIRST = 5;
    public static final int VP_LAND_FIRST = 10;
    public static final int VP_LAND_OTHER = 5;
    public static final int VP_SCAN_FIRST = 3;
    public static final int VP_ANALYZE = 15;

    public static final int COST_LAUNCH_CREDITS = 2;
    public static final int COST_MOVE_ENERGY = 1;
    public static final int COST_ORBIT_CREDITS = 1;
    public static final int COST_ORBIT_ENERGY = 1;
    public static final int COST_LAND_ENERGY = 3;
    public static final int COST_SCAN_CREDITS = 1;
    public static final int COST_SCAN_ENERGY = 2;
    public static final int COST_ANALYZE_TOKENS = 5;
    public static final int COST_ANALYZE_ENERGY = 1;

    public static final int COST_TRADE_RATIO = 2;
    public static final int TRADE_REWARD = 1;

    public static final int REWARD_ORBIT_TOKENS = 3;
    public static final int REWARD_LAND_TOKENS = 2;
    public static final int REWARD_SCAN_TOKENS = 1;

    private GameConfig() {}
}