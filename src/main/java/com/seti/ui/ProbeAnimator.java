package com.seti.ui;

import com.seti.engine.GameState;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class ProbeAnimator {

    private static final double PROBE_SIZE = 28.0;
    private static final double ORBIT_RADIUS = 45.0;
    private static final double SCAN_MIN_RADIUS = 10.0;
    private static final double SCAN_MAX_RADIUS = 55.0;
    private static final long MOVE_DURATION_NANOS = 450_000_000L;
    private static final long ORBIT_DURATION_NANOS = 800_000_000L;
    private static final long RETURN_DURATION_NANOS = 400_000_000L;
    private static final long SCAN_DURATION_NANOS = 600_000_000L;

    private final DynamicBoardCanvas canvas;

    public ProbeAnimator(DynamicBoardCanvas canvas) {
        this.canvas = canvas;
    }

    public void animate(GameState state, int playerIndex,
                        int fromRing, int fromSector,
                        int toRing, int toSector,
                        Runnable onFinished) {
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;
        double[] from = BoardGeometry.getProbePosition(fromRing, fromSector, cx, cy);
        double[] to = BoardGeometry.getProbePosition(toRing, toSector, cx, cy);

        new AnimationTimer() {
            private long startTime = -1;

            @Override
            public void handle(long now) {
                if (startTime < 0) startTime = now;
                double t = Math.min(1.0, (now - startTime) / (double) MOVE_DURATION_NANOS);
                double x = from[0] + (to[0] - from[0]) * t;
                double y = from[1] + (to[1] - from[1]) * t;

                drawProbeFrame(state, playerIndex, x, y);
                if (t >= 1.0) {
                    stop();
                    onFinished.run();
                }
            }
        }.start();
    }

    public void animateOrbit(GameState state, int playerIndex, int ring, int sector, Runnable onFinished) {
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;
        double[] planetPos = BoardGeometry.getCellPosition(ring, sector, cx, cy);
        double[] basePos = {cx, cy};

        new AnimationTimer() {
            private long startTime = -1;

            @Override
            public void handle(long now) {
                if (startTime < 0) startTime = now;
                long elapsed = now - startTime;
                double x, y;

                if (elapsed < ORBIT_DURATION_NANOS) {
                    double t = elapsed / (double) ORBIT_DURATION_NANOS;
                    double angle = t * 2 * Math.PI - Math.PI / 2;
                    x = planetPos[0] + ORBIT_RADIUS * Math.cos(angle);
                    y = planetPos[1] + ORBIT_RADIUS * Math.sin(angle);
                } else if (elapsed < ORBIT_DURATION_NANOS + RETURN_DURATION_NANOS) {
                    double t = (elapsed - ORBIT_DURATION_NANOS) / (double) RETURN_DURATION_NANOS;
                    x = planetPos[0] + (basePos[0] - planetPos[0]) * t;
                    y = planetPos[1] + (basePos[1] - planetPos[1]) * t;
                } else {
                    stop();
                    onFinished.run();
                    return;
                }

                drawProbeFrame(state, playerIndex, x, y);
            }
        }.start();
    }

    public void animateScan(GameState state, int ring, int sector, Runnable onFinished) {
        double cx = canvas.getWidth() / 2;
        double cy = canvas.getHeight() / 2;
        double[] pos = BoardGeometry.getCellPosition(ring, sector, cx, cy);

        new AnimationTimer() {
            private long startTime = -1;

            @Override
            public void handle(long now) {
                if (startTime < 0) startTime = now;
                double t = Math.min(1.0, (now - startTime) / (double) SCAN_DURATION_NANOS);

                canvas.draw(state);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                double radius = SCAN_MIN_RADIUS + t * (SCAN_MAX_RADIUS - SCAN_MIN_RADIUS);
                double alpha = 1.0 - t;
                gc.setStroke(Color.rgb(130, 255, 130, alpha));
                gc.setLineWidth(2.5);
                gc.strokeOval(pos[0] - radius, pos[1] - radius, radius * 2, radius * 2);

                if (t >= 1.0) {
                    stop();
                    onFinished.run();
                }
            }
        }.start();
    }

    private void drawProbeFrame(GameState state, int playerIndex, double x, double y) {
        canvas.draw(state, playerIndex);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(canvas.getProbeImage(playerIndex),
                x - PROBE_SIZE / 2, y - PROBE_SIZE / 2, PROBE_SIZE, PROBE_SIZE);
    }
}