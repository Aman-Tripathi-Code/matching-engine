package com.aman.matchingengine.engine;


import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.Trade;

import java.util.List;

public final class OrderPlacementResult {
    private final Order order;
    private final List<Trade> trades;

    public OrderPlacementResult(Order order, List<Trade> trades) {
        this.order = order;
        this.trades = trades;
    }

    public Order getOrder() {
        return order;
    }
    public List<Trade> getTrades() {
        return trades;
    }

    public boolean hasTrades() {
        return !trades.isEmpty();
    }

    @Override
    public String toString() {
        return "OrderPlacementResult{order=" + order + ", trades=" + trades + "}";
    }
}