package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class AnalyzeAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        return player.getResources().hasDataTokens(GameConfig.COST_ANALYZE_TOKENS)
                && player.getResources().hasEnergy(GameConfig.COST_ANALYZE_ENERGY);
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot analyze — not enough data tokens or energy");
        }
        Player player = state.getCurrentPlayer();
        player.getResources().spendDataTokens(GameConfig.COST_ANALYZE_TOKENS);
        player.getResources().spendEnergy(GameConfig.COST_ANALYZE_ENERGY);
        player.addVictoryPoints(GameConfig.VP_ANALYZE);
        player.addTrail(TrailType.BLUE);
        return ActionResult.success(player.getName() + " analyzed data — Blue trail earned!");
    }
}