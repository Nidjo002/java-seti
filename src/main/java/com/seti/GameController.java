package com.seti;

import com.seti.engine.*;
import com.seti.engine.action.*;
import com.seti.model.GameConfig;
import com.seti.model.Player;
import com.seti.model.Probe;
import com.seti.model.TrailType;
import com.seti.ui.BackgroundCanvas;
import com.seti.ui.DynamicBoardCanvas;
import com.seti.ui.ProbeAnimator;
import com.seti.utils.*;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.seti.engine.action.TradeDirection.CREDITS_FOR_ENERGY;
import static com.seti.engine.action.TradeDirection.ENERGY_FOR_CREDITS;

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

    private GameEngine engine;
    private List<String> playerNames;
    private ProbeAnimator probeAnimator;
    private final List<ReplayEntryUtil> replayLog = new ArrayList<>();

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
        dynamicBoardCanvas.setOnCellSelected((ring, sector) -> updateButtons());
        probeAnimator = new ProbeAnimator(dynamicBoardCanvas);
    }

    public void startNewGame(List<String> names) {
        this.playerNames = names;
        GameState state = new GameInitializer().initializeGame(names);
        engine = new GameEngine(state);
        replayLog.clear();
        logArea.clear();
        backgroundCanvas.drawBoard(state.getCells());
        dynamicBoardCanvas.clearSelection();
        bindProperties();
        dynamicBoardCanvas.draw(state);
        updateButtons();
        log("New game started: " + String.join(" vs ", names));
    }

    public void loadState(GameState state) {
        engine = new GameEngine(state);
        backgroundCanvas.drawBoard(state.getCells());
        dynamicBoardCanvas.clearSelection();
        bindProperties();
        dynamicBoardCanvas.draw(state);
        updateButtons();
        log("Game loaded.");
    }

    private void bindProperties() {
        GameState state = engine.getState();
        roundNumberLabel.textProperty().bind(state.currentRoundProperty().asString());
        roundTotalLabel.setText("/ " + GameConfig.NUM_ROUNDS);
        currentPlayerLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> state.getCurrentPlayer().getName(),
                        state.currentPlayerIndexProperty()));
        bindPlayerProperties(state.getCurrentPlayer());
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

    private void autoSave() {
        AutoSaver.save(engine.getState(),
                () -> log("Auto-saved."),
                ex -> log("Auto-save failed: " + ex.getMessage()));
    }

    private void executeAction(GameAction action) {
        int playerIndex = engine.getState().currentPlayerIndexProperty().get();
        var result = engine.executeAction(action);
        if (result.success()) {
            replayLog.add(new ReplayEntryUtil(playerIndex, action));
            XmlUtils.saveReplay(replayLog);
            autoSave();
        }
        log(result.message());
        dynamicBoardCanvas.clearSelection();
        dynamicBoardCanvas.draw(engine.getState());
        bindPlayerProperties(engine.getState().getCurrentPlayer());
        updateButtons();
        if (engine.getState().isGameOver()) showGameOver();
    }

    private void executeAnimatedAction(GameAction action) {
        int playerIndex = engine.getState().currentPlayerIndexProperty().get();
        Probe probe = engine.getState().getCurrentPlayer().getProbe();
        int fromRing = probe.getProbeCurrentRing();
        int fromSector = probe.getProbeCurrentSector();

        var result = engine.executeAction(action);
        log(result.message());

        if (!result.success()) {
            dynamicBoardCanvas.clearSelection();
            dynamicBoardCanvas.draw(engine.getState());
            updateButtons();
            return;
        }

        replayLog.add(new ReplayEntryUtil(playerIndex, action));
        XmlUtils.saveReplay(replayLog);
        autoSave();

        Probe updatedProbe = engine.getState().getPlayers().get(playerIndex).getProbe();
        int toRing = updatedProbe.getProbeCurrentRing();
        int toSector = updatedProbe.getProbeCurrentSector();

        dynamicBoardCanvas.clearSelection();
        disableAllButtons();
        probeAnimator.animate(engine.getState(), playerIndex, fromRing, fromSector, toRing, toSector, () -> {
            dynamicBoardCanvas.draw(engine.getState());
            bindPlayerProperties(engine.getState().getCurrentPlayer());
            updateButtons();
            if (engine.getState().isGameOver()) showGameOver();
        });
    }

    private void showGameOver() {
        Player winner = engine.getState().getWinner();
        log("=== GAME OVER ===");
        log(winner.getName() + " wins with " + winner.calculateFinalScore() + " points!");
        disableAllButtons();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(winner.getName() + " wins!");
        alert.setContentText("Final score: " + winner.calculateFinalScore() + " victory points.");
        alert.showAndWait();
    }

    private void disableAllButtons() {
        List.of(launchButton, moveButton, orbitButton, landButton,
                        scanButton, analyzeButton, tradeECButton, tradeCEButton, endTurnButton)
                .forEach(b -> b.setDisable(true));
    }

    private void switchToMainMenu() {
        try {
            Parent menuRoot = FXMLLoader.load(getClass().getResource("mainMenu.fxml"));
            logArea.getScene().setRoot(menuRoot);
        } catch (IOException e) {
            log("Failed to return to menu: " + e.getMessage());
        }
    }

    @FXML private void onLaunch() { executeAnimatedAction(launchAction); }

    @FXML
    private void onOrbit() {
        int playerIndex = engine.getState().currentPlayerIndexProperty().get();
        Probe probe = engine.getState().getCurrentPlayer().getProbe();
        int ring = probe.getProbeCurrentRing();
        int sector = probe.getProbeCurrentSector();

        var result = engine.executeAction(orbitAction);
        log(result.message());

        if (!result.success()) {
            dynamicBoardCanvas.clearSelection();
            dynamicBoardCanvas.draw(engine.getState());
            updateButtons();
            return;
        }

        replayLog.add(new ReplayEntryUtil(playerIndex, orbitAction));
        XmlUtils.saveReplay(replayLog);
        autoSave();

        dynamicBoardCanvas.clearSelection();
        disableAllButtons();
        probeAnimator.animateOrbit(engine.getState(), playerIndex, ring, sector, () -> {
            dynamicBoardCanvas.draw(engine.getState());
            bindPlayerProperties(engine.getState().getCurrentPlayer());
            updateButtons();
            if (engine.getState().isGameOver()) showGameOver();
        });
    }

    @FXML private void onMove() {
        executeAnimatedAction(new MoveAction(dynamicBoardCanvas.getSelectedRing(),
                dynamicBoardCanvas.getSelectedSector()));
    }

    @FXML
    private void onScan() {
        Probe probe = engine.getState().getCurrentPlayer().getProbe();
        int ring = probe.getProbeCurrentRing();
        int sector = probe.getProbeCurrentSector();
        int playerIndex = engine.getState().currentPlayerIndexProperty().get();

        var result = engine.executeAction(scanAction);
        log(result.message());

        if (!result.success()) {
            updateButtons();
            return;
        }

        replayLog.add(new ReplayEntryUtil(playerIndex, scanAction));
        XmlUtils.saveReplay(replayLog);
        autoSave();

        disableAllButtons();
        probeAnimator.animateScan(engine.getState(), ring, sector, () -> {
            dynamicBoardCanvas.draw(engine.getState());
            bindPlayerProperties(engine.getState().getCurrentPlayer());
            updateButtons();
            if (engine.getState().isGameOver()) showGameOver();
        });
    }

    @FXML private void onLand()    { executeAction(landAction); }
    @FXML private void onAnalyze() { executeAction(analyzeAction); }
    @FXML private void onTradeEC() { executeAction(tradeEC); }
    @FXML private void onTradeCE() { executeAction(tradeCE); }
    @FXML private void onEndTurn() { executeAction(endTurnAction); }

    @FXML private void onSave() {
        try {
            GameSerializerUtil.createGameSaveFile(engine.getState());
            log("Game saved.");
        } catch (IOException e) {
            log("Failed to save game: " + e.getMessage());
        }
    }

    @FXML private void onLoad() {
        try {
            GameSerializerUtil.loadGameSaveFile()
                    .ifPresentOrElse(this::loadState, () -> log("No save file found."));
        } catch (IOException | ClassNotFoundException e) {
            log("Failed to load game: " + e.getMessage());
        }
    }

    @FXML private void onNewGame()       { switchToMainMenu(); }
    @FXML private void onExit()          { javafx.application.Platform.exit(); }
    @FXML private void onDocumentation() { DocumentationUtils.openDocumentation(); }

    @FXML public void onWatchReplay() {
        List<ReplayEntryUtil> entries = XmlUtils.readReplay();
        if (entries.isEmpty()) { log("No replay to watch."); return; }
        startNewGame(playerNames);
        disableAllButtons();
        log("Replaying " + entries.size() + " actions...");
        Timeline tl = ReplayUtils.buildReplay(entries, engine, e -> {
            dynamicBoardCanvas.draw(e.getState());
            bindPlayerProperties(e.getState().getCurrentPlayer());
        });
        tl.play();
    }
}