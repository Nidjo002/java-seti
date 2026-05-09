package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;
import com.seti.model.*;

public final class TradeAction implements GameAction {

    public enum TradeDirection {
        ENERGY_FOR_CREDITS,
        CREDITS_FOR_ENERGY
    }

    private final TradeDirection direction;

    public TradeAction(TradeDirection direction) {
        this.direction = direction;
    }

    @Override
    public boolean canExecute(GameState state) {
        Player player = state.getCurrentPlayer();
        return switch (direction) {
            case ENERGY_FOR_CREDITS -> player.getResources().hasEnergy(GameConfig.COST_TRADE_RATIO);
            case CREDITS_FOR_ENERGY -> player.getResources().hasCredits(GameConfig.COST_TRADE_RATIO);
        };
    }

    @Override
    public ActionResult execute(GameState state) {
        if (!canExecute(state)) {
            return ActionResult.failure("Cannot trade — not enough resources");
        }
        Player player = state.getCurrentPlayer();
        return switch (direction) {
            case ENERGY_FOR_CREDITS -> {
                player.getResources().spendEnergy(GameConfig.COST_TRADE_RATIO);
                player.getResources().addCredits(GameConfig.TRADE_REWARD);
                yield ActionResult.success(player.getName() + " traded " + GameConfig.COST_TRADE_RATIO + " energy for " + GameConfig.TRADE_REWARD + " credits");
            }
            case CREDITS_FOR_ENERGY -> {
                player.getResources().spendCredits(GameConfig.COST_TRADE_RATIO);
                player.getResources().addEnergy(GameConfig.TRADE_REWARD);
                yield ActionResult.success(player.getName() + " traded " + GameConfig.COST_TRADE_RATIO + " credits for " + GameConfig.TRADE_REWARD + " energy");
            }
        };
    }
}