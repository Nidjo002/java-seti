package com.seti.model;

import javafx.beans.property.*;
import javafx.collections.*;

import java.io.*;
import java.util.List;
import java.io.Serial;

public class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Resource resources;
    private final Probe probe;
    private transient IntegerProperty victoryPoints;
    private transient ListProperty<TrailType> trails;

    public Player(String name) {
        this.name = name;
        this.resources = new Resource();
        this.probe = new Probe();
        this.victoryPoints = new SimpleIntegerProperty(0);
        this.trails = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(victoryPoints.get());
        out.writeObject(trails.get().stream().toList());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        victoryPoints = new SimpleIntegerProperty(in.readInt());
        trails = new SimpleListProperty<>(FXCollections.observableArrayList(
                (List<TrailType>) in.readObject()));
    }

    public void addVictoryPoints(int points) {
        victoryPoints.set(victoryPoints.get() + points);
    }

    public void addTrail(TrailType trail) {
        trails.add(trail);
    }

    public int calculateFinalScore() {
        int trailPoints = trails.stream()
                .mapToInt(TrailType::getVictoryPoints)
                .sum();
        return victoryPoints.get() + trailPoints;
    }


    public String getName() { return name; }
    public Resource getResources() { return resources; }
    public Probe getProbe() { return probe; }

    public IntegerProperty victoryPointsProperty() { return victoryPoints; }
    public ListProperty<TrailType> trailsProperty() { return trails; }


}