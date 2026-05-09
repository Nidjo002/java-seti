package com.seti.ui;

import com.seti.engine.GameState;
import com.seti.model.GameConfig;
import com.seti.model.Player;
import com.seti.model.Probe;
import com.seti.model.Star;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Objects;
import java.util.function.BiConsumer;

public class DynamicBoardCanvas extends Canvas {

    private static final double STAR_RADIUS = 26.0;
    private static final double PROBE_SIZE = 28.0;
    private static final double DEFAULT_SIZE = 900.0;

    private final Image[] probeImages;

    private GameState lastState;
    private int selectedRing = -1;
    private int selectedSector = -1;
    private BiConsumer<Integer, Integer> onCellSelected;

    public DynamicBoardCanvas() {
        this(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public DynamicBoardCanvas(double width, double height) {
        super(width, height);
        this.probeImages = new Image[]{
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/seti/images/probe1.png"))),
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/seti/images/probe2.png")))
        };
        setOnMouseClicked(this::onMouseClicked);
    }

    public void draw(GameState state) {
        this.lastState = state;
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        double cx = getWidth() / 2;
        double cy = getHeight() / 2;
        double starRingRadius = BoardGeometry.SUN_RADIUS
                + GameConfig.NUM_RINGS * BoardGeometry.RING_GAP
                + BoardGeometry.STAR_RING_OFFSET;

        drawStars(gc, state, cx, cy, starRingRadius);
        drawProbes(gc, state, cx, cy);
        drawSelection(gc, cx, cy);
    }

    private void drawStars(GraphicsContext gc, GameState state,
                           double cx, double cy, double starRingRadius) {
        for (Star star : state.getStars()) {
            double angle = BoardGeometry.getSectorMidAngle(star.getSector());
            double x = cx + starRingRadius * Math.cos(angle);
            double y = cy + starRingRadius * Math.sin(angle);
            drawStar(gc, star, x, y);
        }
    }

    private void drawStar(GraphicsContext gc, Star star, double x, double y) {
        gc.setFill(Color.rgb(80, 130, 255, 0.06));
        gc.fillOval(x - STAR_RADIUS - 8, y - STAR_RADIUS - 8,
                (STAR_RADIUS + 8) * 2, (STAR_RADIUS + 8) * 2);

        RadialGradient starGradient = new RadialGradient(0, 0,
                x - STAR_RADIUS * 0.3, y - STAR_RADIUS * 0.3,
                STAR_RADIUS * 1.2, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(200, 220, 255)),
                new Stop(0.4, Color.rgb(80, 120, 230)),
                new Stop(1.0, Color.rgb(20, 45, 140)));
        gc.setFill(starGradient);
        gc.fillOval(x - STAR_RADIUS, y - STAR_RADIUS, STAR_RADIUS * 2, STAR_RADIUS * 2);

        gc.setStroke(Color.rgb(140, 190, 255, 0.85));
        gc.setLineWidth(1.8);
        gc.strokeOval(x - STAR_RADIUS, y - STAR_RADIUS, STAR_RADIUS * 2, STAR_RADIUS * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(star.getName().substring(0, Math.min(5, star.getName().length())), x, y + 7);

        boolean limitReached = star.hasReachedScanLimit();
        gc.setFill(limitReached ? Color.rgb(255, 90, 90) : Color.rgb(130, 255, 130));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(star.getTotalScans() + "/" + GameConfig.MAX_SCANS_PER_SECTOR, x, y + 18);
    }

    private void drawProbes(GraphicsContext gc, GameState state, double cx, double cy) {
        for (int i = 0; i < state.getPlayers().size(); i++) {
            Player player = state.getPlayers().get(i);
            Probe probe = player.getProbe();
            if (probe.isProbeOnMap()) {
                double[] pos = BoardGeometry.getCellPosition(
                        probe.getProbeCurrentRing(), probe.getProbeCurrentSector(), cx, cy);
                double px = pos[0] + (i - state.getPlayers().size() / 2.0) * 14;
                double py = pos[1];
                gc.drawImage(probeImages[i % probeImages.length],
                        px - PROBE_SIZE / 2, py - PROBE_SIZE / 2, PROBE_SIZE, PROBE_SIZE);
            }
        }
    }

    private void drawSelection(GraphicsContext gc, double cx, double cy) {
        if (selectedRing == -1 || selectedSector == -1) return;
        double[] pos = BoardGeometry.getCellPosition(selectedRing, selectedSector, cx, cy);
        double x = pos[0];
        double y = pos[1];
        double r = BoardGeometry.CELL_RADIUS;
        for (int i = 3; i >= 1; i--) {
            gc.setStroke(Color.rgb(255, 255, 255, 0.12 * i));
            gc.setLineWidth(i * 3.0);
            gc.strokeOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
        }
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.8);
        gc.strokeOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
    }

    private void onMouseClicked(MouseEvent event) {
        if (lastState == null) return;
        double cx = getWidth() / 2;
        double cy = getHeight() / 2;

        double dx = event.getX() - cx;
        double dy = event.getY() - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);


        int ring = (int) Math.round((dist - BoardGeometry.SUN_RADIUS) / BoardGeometry.RING_GAP);


        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;
        int sector = (int) (angle / (2 * Math.PI / GameConfig.NUM_SECTORS)) + 1;


        if (ring >= 1 && ring <= GameConfig.NUM_RINGS && sector >= 1 && sector <= GameConfig.NUM_SECTORS) {
            double[] pos = BoardGeometry.getCellPosition(ring, sector, cx, cy);
            if (Math.hypot(event.getX() - pos[0], event.getY() - pos[1]) <= BoardGeometry.CELL_RADIUS + 5) {
                selectedRing = ring;
                selectedSector = sector;
                draw(lastState);
                if (onCellSelected != null) onCellSelected.accept(selectedRing, selectedSector);
                return;
            }
        }

        clearSelection();
        draw(lastState);
    }

    public void clearSelection() {
        selectedRing = -1;
        selectedSector = -1;
    }

    public void setOnCellSelected(BiConsumer<Integer, Integer> callback) {
        this.onCellSelected = callback;
    }

    public int getSelectedRing() { return selectedRing; }
    public int getSelectedSector() { return selectedSector; }
    public boolean hasSelection() { return selectedRing != -1 && selectedSector != -1; }
}