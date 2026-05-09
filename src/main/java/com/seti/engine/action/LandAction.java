package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class LandAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        if (!probe.isProbeOnMap()) return false;
        if (!player.getResources().hasEnergy(GameConfig.COST_LAND_ENERGY)) return false;
        return state.getCell(probe.getProbeCurrentRing(), probe.getProbeCurrentSector()).canLand();
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot land — not enough energy or no planet at current position");
        }
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        Cell cell = state.getCell(probe.getProbeCurrentRing(), probe.getProbeCurrentSector());
        player.getResources().spendEnergy(GameConfig.COST_LAND_ENERGY);
        player.getResources().addDataTokens(GameConfig.REWARD_LAND_TOKENS);
        player.addTrail(TrailType.YELLOW);
        boolean isFirst = cell.planet().claimFirstLanding();
        if (isFirst) {
            player.addVictoryPoints(GameConfig.VP_LAND_FIRST);
        } else {
            player.addVictoryPoints(GameConfig.VP_LAND_OTHER);
        }
        probe.resetProbe();
        return ActionResult.success(player.getName() + " landed on " + cell.planet().getName()
                + (isFirst ? " — first landing!" : ""));
    }
}