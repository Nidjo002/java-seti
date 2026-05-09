package com.seti.engine;

import com.seti.model.*;

import java.util.Arrays;
import java.util.List;

public class GameInitializer {

    public GameState initializeGame(List<String> playerNames) {
        validatePlayers(playerNames);
        List<Player> players = createPlayers(playerNames);
        List<Cell> cells = createCells();
        List<Star> stars = createStars();
        return new GameState(players, cells, stars);
    }

    private void validatePlayers(List<String> playerNames) {
        if (playerNames.size() < GameConfig.MIN_PLAYERS) {
            throw new IllegalArgumentException(
                    "Minimum " + GameConfig.MIN_PLAYERS + " players required");
        }
        if (playerNames.size() > GameConfig.MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Maximum " + GameConfig.MAX_PLAYERS + " players allowed");
        }

        long uniqueNames = playerNames.stream().distinct().count();
        if (uniqueNames != playerNames.size()) {
            throw new IllegalArgumentException("Players must have unique names");
        }
    }

    private List<Player> createPlayers(List<String> playerNames) {
        return playerNames.stream()
                .map(Player::new)
                .toList();
    }

    private List<Cell> createCells() {
        List<Cell> cells = new java.util.ArrayList<>();
        for (int ring = 1; ring <= GameConfig.NUM_RINGS; ring++) {
            for (int sector = 1; sector <= GameConfig.NUM_SECTORS; sector++) {
                cells.add(createCell(ring, sector));
            }
        }
        return cells;
    }

    private Cell createCell(int ring, int sector) {
        return Arrays.stream(PlanetType.values())
                .filter(p -> p.getRing() == ring && p.getSector() == sector)
                .findFirst()
                .map(p -> new Cell(ring, sector, new Planet(p)))
                .orElse(new Cell(ring, sector));
    }

    private List<Star> createStars() {
        return Arrays.stream(StarType.values())
                .map(Star::new)
                .toList();
    }
}