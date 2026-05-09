package com.seti.engine.action;

import com.seti.engine.ActionResult;
import com.seti.engine.GameState;

public sealed interface GameAction
        permits LaunchAction,
        MoveAction,
        OrbitAction,
        LandAction,
        ScanAction,
        AnalyzeAction,
        TradeAction,
        EndTurnAction {

    ActionResult execute(GameState state);
    boolean canExecute(GameState state);
}