package com.seti.ui;

import com.seti.engine.GameEngine;
import com.seti.engine.GameInitializer;
import com.seti.engine.GameState;
import com.seti.engine.action.*;
import com.seti.model.GameConfig;
import com.seti.model.Player;
import com.seti.model.TrailType;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.seti.engine.action.TradeAction.TradeDirection.*;

public class GameController {

    @FXML private BackgroundCanvas backgroundCanvas;
    @FXML private DynamicBoardCanvas dynamicBoardCanvas;
    @FXML private Label roundNumberLabel;
    @FXML private Label roundTotalLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label creditsLabel;
    @FXML private Label energyLabel;
    @FXML private Label dataTokensLabel;
    @FXML private Label vpLabel;
    @FXML private Label trailsLabel;
    @FXML private Button launchButton;
    @FXML private Button moveButton;
    @FXML private Button orbitButton;
    @FXML private Button landButton;
    @FXML private Button scanButton;
    @FXML private Button analyzeButton;
    @FXML private Button tradeECButton;
    @FXML private Button tradeCEButton;
    @FXML private Button endTurnButton;
    @FXML private TextArea logArea;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;

    private GameEngine engine;

    private final LaunchAction launchAction = new LaunchAction();
    private final OrbitAction orbitAction = new OrbitAction();
    private final LandAction landAction = new LandAction();
    private final ScanAction scanAction = new ScanAction();
    private final AnalyzeAction analyzeAction = new AnalyzeAction();
    private final EndTurnAction endTurnAction = new EndTurnAction();
    private final TradeAction tradeEC = new TradeAction(ENERGY_FOR_CREDITS);
    private final TradeAction tradeCE = new TradeAction(CREDITS_FOR_ENERGY);

    @FXML
    public void initialize() {
        GameState state = new GameInitializer().initializeGame(List.of("Player 1", "Player 2"));
        engine = new GameEngine(state);
        backgroundCanvas.drawBoard(state.getCells());
        dynamicBoardCanvas.setOnCellSelected((ring, sector) -> updateButtons());
        bindProperties();
        dynamicBoardCanvas.draw(state);
        updateButtons();
    }

    private void bindProperties() {
        GameState state = engine.getState();
        roundNumberLabel.textProperty().bind(state.currentRoundProperty().asString());
        roundTotalLabel.setText("/ " + GameConfig.NUM_ROUNDS);
        currentPlayerLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> state.getCurrentPlayer().getName(),
                        state.currentPlayerIndexProperty()
                )
        );
        bindPlayerProperties(state.getCurrentPlayer());
        state.currentPlayerIndexProperty().addListener((_, _, _) -> {
            bindPlayerProperties(state.getCurrentPlayer());
            updateButtons();
        });
    }

    private void bindPlayerProperties(Player player) {
        creditsLabel.textProperty().bind(player.getResources().creditsProperty().asString());
        energyLabel.textProperty().bind(player.getResources().energyProperty().asString());
        dataTokensLabel.textProperty().bind(player.getResources().dataTokensProperty().asString());
        vpLabel.textProperty().bind(player.victoryPointsProperty().asString());
        trailsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    String trails = player.trailsProperty().stream()
                            .map(TrailType::getDisplayName)
                            .collect(Collectors.joining(", "));
                    return trails.isEmpty() ? "—" : trails;
                },
                player.trailsProperty()
        ));
    }

    private void updateButtons() {
        GameState state = engine.getState();
        launchButton.setDisable(!launchAction.canExecute(state));
        moveButton.setDisable(!dynamicBoardCanvas.hasSelection()
                || !new MoveAction(dynamicBoardCanvas.getSelectedRing(),
                dynamicBoardCanvas.getSelectedSector()).canExecute(state));
        orbitButton.setDisable(!orbitAction.canExecute(state));
        landButton.setDisable(!landAction.canExecute(state));
        scanButton.setDisable(!scanAction.canExecute(state));
        analyzeButton.setDisable(!analyzeAction.canExecute(state));
        tradeECButton.setDisable(!tradeEC.canExecute(state));
        tradeCEButton.setDisable(!tradeCE.canExecute(state));
        endTurnButton.setDisable(!endTurnAction.canExecute(state));
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private void executeAction(GameAction action) {
        var result = engine.executeAction(action);
        log(result.message());
        dynamicBoardCanvas.clearSelection();
        dynamicBoardCanvas.draw(engine.getState());
        updateButtons();
        if (engine.getState().isGameOver()) {
            showGameOver();
        }
    }

    private void showGameOver() {
        Player winner = engine.getState().getWinner();
        log("=== GAME OVER ===");
        log(winner.getName() + " wins with " + winner.calculateFinalScore() + " points!");
        disableAllButtons();
    }

    private void disableAllButtons() {
        List.of(launchButton, moveButton, orbitButton, landButton,
                        scanButton, analyzeButton, tradeECButton, tradeCEButton, endTurnButton)
                .forEach(b -> b.setDisable(true));
    }

    @FXML private void onLaunch() { executeAction(launchAction); }
    @FXML private void onMove() {
        executeAction(new MoveAction(dynamicBoardCanvas.getSelectedRing(),
                dynamicBoardCanvas.getSelectedSector()));
    }
    @FXML private void onOrbit() { executeAction(orbitAction); }
    @FXML private void onLand() { executeAction(landAction); }
    @FXML private void onScan() { executeAction(scanAction); }
    @FXML private void onAnalyze() { executeAction(analyzeAction); }
    @FXML private void onTradeEC() { executeAction(tradeEC); }
    @FXML private void onTradeCE() { executeAction(tradeCE); }
    @FXML private void onEndTurn() { executeAction(endTurnAction); }

    @FXML private void onSave() {}
    @FXML private void onLoad() {}
    @FXML private void onExit() { javafx.application.Platform.exit(); }
    @FXML private void onRules() {}
    @FXML private void onSendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("You: " + message + "\n");
            chatInput.clear();
        }
    }
}