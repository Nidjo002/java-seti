package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.GameConfig;
import com.seti.model.Player;
import com.seti.model.Probe;

public final class MoveAction implements GameAction {

    private final int targetRing;
    private final int targetSector;

    public MoveAction(int targetRing, int targetSector) {
        this.targetRing = targetRing;
        this.targetSector = targetSector;
    }

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        Probe probe = player.getProbe();
        return probe.isProbeOnMap()
                && player.getResources().hasEnergy(GameConfig.COST_MOVE_ENERGY)
                && isAdjacentCell(probe.getProbeCurrentRing(), probe.getProbeCurrentSector());
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot move probe — not enough energy or invalid position");
        }
        Player player = state.getCurrentPlayer();
        player.getResources().spendEnergy(GameConfig.COST_MOVE_ENERGY);
        player.getProbe().moveTo(targetRing, targetSector);
        return ActionResult.success(player.getName() + " moved probe to ring "
                + targetRing + ", sector " + targetSector);
    }

    private boolean isAdjacentCell(int currentRing, int currentSector) {
        int ringDiff = Math.abs(targetRing - currentRing);
        int sectorDiff = Math.abs(targetSector - currentSector);
        int sectorDiffWrapped = Math.min(sectorDiff, GameConfig.NUM_SECTORS - sectorDiff);
        return (ringDiff == 1 && sectorDiffWrapped == 0)
                || (ringDiff == 0 && sectorDiffWrapped == 1);
    }
}