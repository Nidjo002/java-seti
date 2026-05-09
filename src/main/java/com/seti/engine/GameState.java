package com.seti.engine;

import com.seti.model.Cell;
import com.seti.model.GameConfig;
import com.seti.model.Player;
import com.seti.model.Star;
import javafx.beans.property.*;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Player> players;
    private final List<Cell> cells;
    private final List<Star> stars;

    private transient IntegerProperty currentRound;
    private transient IntegerProperty currentPlayerIndex;
    private transient ObjectProperty<GamePhase> currentPhase;

    public GameState(List<Player> players, List<Cell> cells, List<Star> stars) {
        this.players = players;
        this.cells = cells;
        this.stars = stars;
        this.currentRound = new SimpleIntegerProperty(1);
        this.currentPlayerIndex = new SimpleIntegerProperty(0);
        this.currentPhase = new SimpleObjectProperty<>(GamePhase.PLAYER_TURN);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(currentRound.get());
        out.writeInt(currentPlayerIndex.get());
        out.writeObject(currentPhase.get());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        currentRound = new SimpleIntegerProperty(in.readInt());
        currentPlayerIndex = new SimpleIntegerProperty(in.readInt());
        currentPhase = new SimpleObjectProperty<>((GamePhase) in.readObject());
    }

    public void startGame() {
        currentPhase.set(GamePhase.PLAYER_TURN);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex.get());
    }

    public void nextPlayer() {
        int next = (currentPlayerIndex.get() + 1) % players.size();
        currentPlayerIndex.set(next);
        if (next == 0) {
            if (currentRound.get() >= GameConfig.NUM_ROUNDS) {
                currentPhase.set(GamePhase.GAME_OVER);
            } else {
                currentRound.set(currentRound.get() + 1);
            }
        }
    }

    public boolean isGameOver() {
        return currentPhase.get() == GamePhase.GAME_OVER;
    }

    public Cell getCell(int ring, int sector) {
        return cells.stream()
                .filter(c -> c.ring() == ring && c.sector() == sector)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cell not found: ring=" + ring + ", sector=" + sector));
    }

    public Star getStarBySector(int sector) {
        return stars.stream()
                .filter(s -> s.getSector() == sector)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Star not found for sector: " + sector));
    }

    public Player getWinner() {
        return players.stream()
                .max(Comparator.comparingInt(Player::calculateFinalScore))
                .orElseThrow();
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Cell> getCells() { return Collections.unmodifiableList(cells); }
    public List<Star> getStars() { return Collections.unmodifiableList(stars); }

    public IntegerProperty currentRoundProperty() { return currentRound; }
    public IntegerProperty currentPlayerIndexProperty() { return currentPlayerIndex; }
    public ObjectProperty<GamePhase> currentPhaseProperty() { return currentPhase; }
}