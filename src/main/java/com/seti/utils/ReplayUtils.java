package com.seti.utils;

import com.seti.engine.GameEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public final class ReplayUtils {

    private ReplayUtils() {
    }

    private static final double STEP_SECONDS = 1.0;

    public static Timeline buildReplay(List<ReplayEntryUtil> entries, GameEngine engine,
                                       Consumer<GameEngine> onStep) {
        AtomicInteger index = new AtomicInteger(0);

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(STEP_SECONDS),
                event -> {
                    ReplayEntryUtil entry = entries.get(index.getAndIncrement());
                    engine.executeAction(entry.action());
                    onStep.accept(engine);
                }));

        timeline.setCycleCount(entries.size());
        return timeline;
    }
}