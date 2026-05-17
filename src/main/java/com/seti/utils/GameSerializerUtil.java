package com.seti.utils;

import com.seti.engine.GameInitializer;
import com.seti.engine.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public final class GameSerializerUtil {

    private static final Path SAVE_FILE = Path.of("dat", "game_save.dat");
    private static final Path TEMP_FILE = Path.of("dat", "game_save.tmp");
    private static final Path BACKUP_FILE = Path.of("dat", "game_save_backup.dat");
    private static final ObjectInputFilter SAFE_FILTER =
            ObjectInputFilter.Config.createFilter(
                    "com.seti.**;" +
                            "java.util.ArrayList;" +
                            "java.util.HashMap;" +
                            "java.util.List;" +
                            "java.lang.String;" +
                            "!*"
            );

    private GameSerializerUtil() {}

    public static void createGameSaveFile(GameState gameState) throws IOException {
        Files.createDirectories(SAVE_FILE.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(TEMP_FILE))) {
            oos.writeObject(gameState);
        }
        if (Files.exists(SAVE_FILE)) {
            Files.copy(SAVE_FILE, BACKUP_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(TEMP_FILE, SAVE_FILE, StandardCopyOption.ATOMIC_MOVE);
    }

    public static Optional<GameState> loadGameSaveFile() throws IOException, ClassNotFoundException {
        if (!Files.exists(SAVE_FILE)) {
            return Optional.empty();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(SAVE_FILE))) {
            ois.setObjectInputFilter(SAFE_FILTER);
            return Optional.of((GameState) ois.readObject());
        }
    }

    public static GameState newGame() {
        return new GameInitializer().initializeGame(List.of("Player 1", "Player 2"));
    }
}