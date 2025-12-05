package com.avilix.eco.currency;

import java.util.UUID;

public class PlayerBalance {
    private final UUID playerId;
    private double amount;
    private long lastUpdated;

    public PlayerBalance(UUID playerId, double initialAmount) {
        this.playerId = playerId;
        this.amount = initialAmount;
        this.lastUpdated = System.currentTimeMillis();
    }

    public double getAmount() {
        return amount;
    }

    public boolean has(double required) {
        return amount >= required;
    }

    public double add(double addAmount) {
        this.amount += addAmount;
        this.lastUpdated = System.currentTimeMillis();
        return this.amount;
    }

    public double remove(double removeAmount) {
        this.amount -= removeAmount;
        this.lastUpdated = System.currentTimeMillis();
        return this.amount;
    }

    public void set(double newAmount) {
        this.amount = newAmount;
        this.lastUpdated = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}