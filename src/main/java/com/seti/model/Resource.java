package com.seti.model;

import javafx.beans.property.*;

import java.io.*;

public class Resource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient IntegerProperty credits;
    private transient IntegerProperty energy;
    private transient IntegerProperty dataTokens;

    public Resource() {
        this.credits = new SimpleIntegerProperty(GameConfig.STARTING_CREDITS);
        this.energy = new SimpleIntegerProperty(GameConfig.STARTING_ENERGY);
        this.dataTokens = new SimpleIntegerProperty(GameConfig.STARTING_DATA_TOKENS);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(credits.get());
        out.writeInt(energy.get());
        out.writeInt(dataTokens.get());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        credits = new SimpleIntegerProperty(in.readInt());
        energy = new SimpleIntegerProperty(in.readInt());
        dataTokens = new SimpleIntegerProperty(in.readInt());
    }

    public boolean hasCredits(int amount) { return credits.get() >= amount; }
    public boolean hasEnergy(int amount) { return energy.get() >= amount; }
    public boolean hasDataTokens(int amount) { return dataTokens.get() >= amount; }

    public void spendCredits(int amount) { credits.set(credits.get() - amount); }
    public void spendEnergy(int amount) { energy.set(energy.get() - amount); }
    public void spendDataTokens(int amount) { dataTokens.set(dataTokens.get() - amount); }

    public void addCredits(int amount) { credits.set(credits.get() + amount); }
    public void addEnergy(int amount) { energy.set(energy.get() + amount); }
    public void addDataTokens(int amount) { dataTokens.set(dataTokens.get() + amount); }



    public IntegerProperty creditsProperty() { return credits; }
    public IntegerProperty energyProperty() { return energy; }
    public IntegerProperty dataTokensProperty() { return dataTokens; }
}