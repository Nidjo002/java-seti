package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class EndTurnAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        return !state.isGameOver();
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Game is already over");
        }
        Player player = state.getCurrentPlayer();
        player.getResources().addCredits(GameConfig.INCOME_CREDITS);
        player.getResources().addEnergy(GameConfig.INCOME_ENERGY);
        state.nextPlayer();
        return ActionResult.success(player.getName() + " ended their turn");
    }
}