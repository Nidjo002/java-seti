package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class OrbitAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        if (!probe.isProbeOnMap()) return false;
        if (!player.getResources().hasCredits(GameConfig.COST_ORBIT_CREDITS)) return false;
        if (!player.getResources().hasEnergy(GameConfig.COST_ORBIT_ENERGY)) return false;
        return state.getCell(probe.getProbeCurrentRing(), probe.getProbeCurrentSector()).canOrbit();
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot orbit — not enough resources or no planet at current position");
        }
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        Cell cell = state.getCell(probe.getProbeCurrentRing(), probe.getProbeCurrentSector());
        player.getResources().spendCredits(GameConfig.COST_ORBIT_CREDITS);
        player.getResources().spendEnergy(GameConfig.COST_ORBIT_ENERGY);
        player.getResources().addDataTokens(GameConfig.REWARD_ORBIT_TOKENS);
        boolean isFirst = cell.planet().claimFirstOrbit();
        if (isFirst) {
            player.addVictoryPoints(GameConfig.VP_ORBIT_FIRST);
        }
        probe.resetProbe();
        return ActionResult.success(player.getName() + " orbited " + cell.planet().getName()
                + (isFirst ? " — first orbit!" : ""));
    }
}