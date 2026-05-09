package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.GameConfig;
import com.seti.model.Player;

public final class LaunchAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        return !player.getProbe().isProbeOnMap()
                && player.getResources().hasCredits(GameConfig.COST_LAUNCH_CREDITS);
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot launch probe — not enough credits or probe already on map");
        }
        Player player = state.getCurrentPlayer();
        player.getResources().spendCredits(GameConfig.COST_LAUNCH_CREDITS);
        player.getProbe().launchProbe();
        return ActionResult.success(player.getName() + " launched a probe!");
    }
}