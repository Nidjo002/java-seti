package com.seti.utils;

import com.seti.engine.GameInitializer;
import com.seti.engine.GameState;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

public final class GameSerializerUtil {

    private static final Logger logger = Logger.getLogger(GameSerializerUtil.class.getName());
    private static final String SAVE_FILE = "./dat/game_save.dat";

    private GameSerializerUtil() {}

    public static void createGameSaveFile(GameState gameState) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(gameState);
        } catch (IOException e) {
            logger.severe("Failed to save game to: " + SAVE_FILE + " - " + e.getMessage());
        }
    }

    public static GameState loadGameSaveFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameState) ois.readObject();
        } catch (IOException e) {
            logger.severe("Failed to load game from: " + SAVE_FILE + " - " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.severe("Save file contains unknown class: " + e.getMessage());
        }
        return null;
    }

    public static GameState newGame() {
        return new GameInitializer().initializeGame(List.of("Player 1", "Player 2"));
    }

}