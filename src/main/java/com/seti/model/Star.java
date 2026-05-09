package com.seti.model;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Star implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final StarType type;
    private final Map<String, Integer> scansByPlayer;
    private String lastScannedPlayerName;

    public Star(StarType type) {
        this.type = type;
        this.scansByPlayer = new HashMap<>();
    }

    public boolean addScan(String playerName) {
        boolean isFirst = scansByPlayer.isEmpty();
        scansByPlayer.merge(playerName, 1, Integer::sum);
        lastScannedPlayerName = playerName;
        return isFirst;
    }

    public int getTotalScans() {
        return scansByPlayer.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean hasReachedScanLimit() {
        return getTotalScans() >= GameConfig.MAX_SCANS_PER_SECTOR;
    }

    public String getPinkTrailWinner() {
        if (scansByPlayer.isEmpty()) return null;
        int maxScans = Collections.max(scansByPlayer.values());
        List<String> leaders = scansByPlayer.entrySet().stream()
                .filter(e -> e.getValue() == maxScans)
                .map(Map.Entry::getKey)
                .toList();
        return leaders.size() == 1 ? leaders.getFirst() : lastScannedPlayerName;
    }

    public String getName() { return type.getDisplayName(); }
    public int getSector() { return type.getSector(); }
    public StarType getType() { return type; }
    public Map<String, Integer> getScansByPlayer() {
        return Collections.unmodifiableMap(scansByPlayer);
    }
}