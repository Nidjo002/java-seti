package com.seti.utils;

import com.seti.engine.GameState;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public final class AutoSaver {

    private AutoSaver() {}

    public static void save(GameState state, Runnable onSuccess, Consumer<Throwable> onFailure) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GameSerializerUtil.createGameSaveFile(state);
                return null;
            }
        };
        task.setOnSucceeded(_ -> onSuccess.run());
        task.setOnFailed(_ -> onFailure.accept(task.getException()));

        Thread thread = new Thread(task, "auto-save-thread");
        thread.setDaemon(true);
        thread.start();
    }
}