package com.aman.matchingengine.model;

public enum OrderSide {
    BUY, SELL;

    public boolean isBuy() {
        return this == BUY;
    }

    public boolean isSell() {
        return this == SELL;
    }
}