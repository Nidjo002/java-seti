package com.seti.ui;

import com.seti.model.Cell;
import com.seti.model.GameConfig;
import com.seti.model.PlanetType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class BackgroundCanvas extends Canvas {

    private static final Logger logger = Logger.getLogger(BackgroundCanvas.class.getName());
    private static final int NUM_BG_STARS = 250;
    private static final int SEED = 42;
    private static final double EMPTY_CELL_RATIO = 0.38;
    private static final double LABEL_OFFSET = 14.0;
    private static final double DEFAULT_SIZE = 900.0;

    private final double[][] backgroundStars;
    private final Image sunImage;
    private final Map<PlanetType, Image> planetImages = new EnumMap<>(PlanetType.class);

    public BackgroundCanvas() {
        this(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public BackgroundCanvas(double width, double height) {
        super(width, height);
        this.backgroundStars = generateStars();
        this.sunImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/com/seti/images/sun.png")));
        loadPlanetImages();
    }

    private double[][] generateStars() {
        Random rng = new Random(SEED);
        double[][] stars = new double[NUM_BG_STARS][4];
        for (int i = 0; i < NUM_BG_STARS; i++) {
            stars[i][0] = rng.nextDouble();
            stars[i][1] = rng.nextDouble();
            stars[i][2] = 0.3 + rng.nextDouble() * 0.7;
            stars[i][3] = 1.0 + rng.nextDouble() * 2.5;
        }
        return stars;
    }

    private void loadPlanetImages() {
        for (PlanetType planet : PlanetType.values()) {
            try {
                Image img = new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream(
                                "/com/seti/images/" + planet.name().toLowerCase() + ".png")));
                planetImages.put(planet, img);
            } catch (Exception _) {
                logger.warning("Could not load image for: " + planet.name());
            }
        }
    }

    public void drawBoard(List<Cell> cells) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        double cx = getWidth() / 2;
        double cy = getHeight() / 2;
        double starRingRadius = BoardGeometry.SUN_RADIUS
                + GameConfig.NUM_RINGS * BoardGeometry.RING_GAP
                + BoardGeometry.STAR_RING_OFFSET;
        double maxRadius = starRingRadius + 36;

        drawBackground(gc);
        drawNebula(gc, cx, cy);
        drawBackgroundStars(gc);
        drawSectorLines(gc, cx, cy, maxRadius);
        drawRings(gc, cx, cy, starRingRadius);
        drawSun(gc, cx, cy);
        drawCells(gc, cells, cx, cy);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(3, 5, 18));
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawNebula(GraphicsContext gc, double cx, double cy) {
        RadialGradient nebula1 = new RadialGradient(0, 0, cx - 150, cy - 100, 350,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(60, 10, 100, 0.12)),
                new Stop(1, Color.rgb(0, 0, 0, 0)));
        gc.setFill(nebula1);
        gc.fillOval(cx - 450, cy - 400, 700, 600);

        RadialGradient nebula2 = new RadialGradient(0, 0, cx + 200, cy + 150, 280,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(10, 40, 100, 0.10)),
                new Stop(1, Color.rgb(0, 0, 0, 0)));
        gc.setFill(nebula2);
        gc.fillOval(cx - 50, cy - 100, 550, 500);
    }

    private void drawBackgroundStars(GraphicsContext gc) {
        for (double[] star : backgroundStars) {
            double x = star[0] * getWidth();
            double y = star[1] * getHeight();
            double brightness = star[2];
            double size = star[3];
            gc.setFill(Color.rgb(255, 255, 255, brightness));
            gc.fillOval(x, y, size, size);
            if (size > 2.5) {
                gc.setFill(Color.rgb(255, 255, 255, brightness * 0.25));
                gc.fillOval(x - 1.5, y - 1.5, size + 3, size + 3);
            }
        }
    }

    private void drawSectorLines(GraphicsContext gc, double cx, double cy, double maxRadius) {
        gc.setStroke(Color.rgb(80, 120, 200, 0.15));
        gc.setLineWidth(0.8);
        for (int sector = 1; sector <= GameConfig.NUM_SECTORS; sector++) {
            double angle = (sector - 1) * 2 * Math.PI / GameConfig.NUM_SECTORS;
            double x = cx + maxRadius * Math.cos(angle);
            double y = cy + maxRadius * Math.sin(angle);
            gc.strokeLine(cx, cy, x, y);
        }
    }

    private void drawRings(GraphicsContext gc, double cx, double cy, double starRingRadius) {
        Color[] ringColors = {
                Color.rgb(120, 180, 255, 0.35),
                Color.rgb(140, 195, 255, 0.30),
                Color.rgb(160, 210, 255, 0.25),
                Color.rgb(180, 220, 255, 0.20)
        };
        for (int ring = 1; ring <= GameConfig.NUM_RINGS; ring++) {
            double radius = BoardGeometry.SUN_RADIUS + ring * BoardGeometry.RING_GAP;
            gc.setStroke(ringColors[ring - 1]);
            gc.setLineWidth(1.0);
            gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }
        gc.setStroke(Color.rgb(60, 90, 220, 0.65));
        gc.setLineWidth(2.5);
        gc.strokeOval(cx - starRingRadius, cy - starRingRadius,
                starRingRadius * 2, starRingRadius * 2);
        gc.setStroke(Color.rgb(100, 140, 255, 0.25));
        gc.setLineWidth(7.0);
        gc.strokeOval(cx - starRingRadius, cy - starRingRadius,
                starRingRadius * 2, starRingRadius * 2);
    }

    private void drawSun(GraphicsContext gc, double cx, double cy) {
        for (int i = 3; i >= 1; i--) {
            double glowRadius = BoardGeometry.SUN_RADIUS + i * 14;
            gc.setFill(Color.rgb(255, 180, 30, 0.06 * i));
            gc.fillOval(cx - glowRadius, cy - glowRadius, glowRadius * 2, glowRadius * 2);
        }
        gc.drawImage(sunImage, cx - BoardGeometry.SUN_RADIUS, cy - BoardGeometry.SUN_RADIUS,
                BoardGeometry.SUN_RADIUS * 2, BoardGeometry.SUN_RADIUS * 2);
    }

    private void drawCells(GraphicsContext gc, List<Cell> cells, double cx, double cy) {
        for (Cell cell : cells) {
            double[] pos = BoardGeometry.getCellPosition(cell.ring(), cell.sector(), cx, cy);
            if (cell.hasPlanet()) {
                drawPlanet(gc, cell, pos[0], pos[1]);
            } else {
                drawEmptyCell(gc, pos[0], pos[1]);
            }
        }
    }

    private void drawPlanet(GraphicsContext gc, Cell cell, double x, double y) {
        double r = BoardGeometry.CELL_RADIUS;
        Image img = planetImages.get(cell.planet().getType());
        if (img != null) {
            gc.drawImage(img, x - r, y - r, r * 2, r * 2);
        } else {
            gc.setFill(Color.GRAY);
            gc.fillOval(x - r, y - r, r * 2, r * 2);
        }
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(cell.planet().getName(), x, y + r + LABEL_OFFSET);
    }

    private void drawEmptyCell(GraphicsContext gc, double x, double y) {
        double r = BoardGeometry.CELL_RADIUS;
        gc.setFill(Color.rgb(70, 110, 190, 0.12));
        gc.fillOval(x - r * EMPTY_CELL_RATIO, y - r * EMPTY_CELL_RATIO,
                r * EMPTY_CELL_RATIO * 2, r * EMPTY_CELL_RATIO * 2);
        gc.setStroke(Color.rgb(90, 140, 210, 0.18));
        gc.setLineWidth(0.7);
        gc.strokeOval(x - r * EMPTY_CELL_RATIO, y - r * EMPTY_CELL_RATIO,
                r * EMPTY_CELL_RATIO * 2, r * EMPTY_CELL_RATIO * 2);
    }
}