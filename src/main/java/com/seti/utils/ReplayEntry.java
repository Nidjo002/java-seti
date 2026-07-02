package com.seti.utils;

import com.seti.engine.action.GameAction;

import java.util.Objects;


public record ReplayEntry(int playerIndex, GameAction action) {

    public ReplayEntry {
        Objects.requireNonNull(action, "action must not be null");
        if (playerIndex < 0) {
            throw new IllegalArgumentException("playerIndex must not be negative: " + playerIndex);
        }
    }
}