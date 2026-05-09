package com.seti.engine;

import com.seti.engine.action.GameAction;


public class GameEngine {

    private final GameState state;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public ActionResult executeAction(GameAction action) {
        if (state.isGameOver()) {
            return ActionResult.failure("Game is over");
        }
        return action.execute(state);
    }

    public GameState getState() {
        return state;
    }
}