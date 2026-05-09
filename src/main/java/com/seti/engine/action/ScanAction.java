package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class ScanAction implements GameAction {

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        if (!probe.isProbeOnMap()) return false;
        if (!player.getResources().hasCredits(GameConfig.COST_SCAN_CREDITS)) return false;
        if (!player.getResources().hasEnergy(GameConfig.COST_SCAN_ENERGY)) return false;
        Star star = state.getStarBySector(probe.getProbeCurrentSector());
        return !star.hasReachedScanLimit();
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot scan — not enough resources or scan limit reached");
        }
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        Star star = state.getStarBySector(probe.getProbeCurrentSector());
        player.getResources().spendCredits(GameConfig.COST_SCAN_CREDITS);
        player.getResources().spendEnergy(GameConfig.COST_SCAN_ENERGY);
        player.getResources().addDataTokens(GameConfig.REWARD_SCAN_TOKENS);
        boolean isFirst = star.addScan(player.getName());
        if (isFirst) {
            player.addVictoryPoints(GameConfig.VP_SCAN_FIRST);
        }
        boolean pinkTrailAwarded = false;
        if (star.hasReachedScanLimit()) {
            String winnerName = star.getPinkTrailWinner();
            state.getPlayers().stream()
                    .filter(p -> p.getName().equals(winnerName))
                    .findFirst()
                    .ifPresent(p -> {
                        p.addTrail(TrailType.PINK);
                    });
            pinkTrailAwarded = true;
        }
        return ActionResult.success(player.getName() + " scanned " + star.getName()
                + (isFirst ? " — first scan!" : "")
                + (pinkTrailAwarded ? " — Pink trail awarded to " + star.getPinkTrailWinner() + "!" : ""));
    }
}