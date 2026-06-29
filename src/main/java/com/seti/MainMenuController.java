package com.seti;

import com.seti.engine.GameState;
import com.seti.utils.GameSerializerUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainMenuController {

    @FXML private VBox root;
    @FXML private VBox menuBox;
    @FXML private VBox newGameBox;
    @FXML private TextField field1;
    @FXML private TextField field2;

    public void onNewGame() {
        show(newGameBox);
        field1.clear();
        field2.clear();
        field1.requestFocus();
    }

    @FXML
    private void onBack() {
        show(menuBox);
    }

    @FXML
    private void onStart() {
        String name1 = field1.getText().trim();
        String name2 = field2.getText().trim();

        if (name1.isBlank() || name2.isBlank()) {
            showError("Both player names are required.");
            return;
        }
        if (name1.equalsIgnoreCase(name2)) {
            showError("Player names must be different.");
            return;
        }

        launchNewGame(List.of(name1, name2));
    }

    @FXML
    private void onLoad() {
        try {
            Optional<GameState> saved = GameSerializerUtil.loadGameSaveFile();
            if (saved.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No saved game found.").showAndWait();
                return;
            }
            launchWithState(saved.get());
        } catch (IOException | ClassNotFoundException e) {
            showError("Failed to load game: " + e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        javafx.application.Platform.exit();
    }

    private void launchNewGame(List<String> names) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameView.fxml"));
            Parent gameRoot = loader.load();
            GameController controller = loader.getController();
            controller.startNewGame(names);
            switchRoot(gameRoot);
        } catch (IOException e) {
            showError("Failed to start game: " + e.getMessage());
        }
    }

    private void launchWithState(GameState state) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameView.fxml"));
            Parent gameRoot = loader.load();
            GameController controller = loader.getController();
            controller.loadState(state);
            switchRoot(gameRoot);
        } catch (IOException e) {
            showError("Failed to load game: " + e.getMessage());
        }
    }

    private void show(VBox box) {
        for (VBox b : List.of(menuBox, newGameBox)) {
            b.setVisible(b == box);
            b.setManaged(b == box);
        }
    }

    private void switchRoot(Parent newRoot) {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}