package com.aman.matchingengine.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public final class Trade {
    private final String tradeId;
    private final String symbol;
    private final String buyOrderId;
    private final String sellOrderId;
    private final long price;
    private final long quantity;
    private final Instant executedAt;

    public Trade(
            String tradeId,
            String symbol,
            String buyOrderId,
            String sellOrderId,
            long price,
            long quantity,
            Instant executedAt
    ) {
        this.tradeId = validateNonBlank(tradeId, "tradeId");
        this.symbol = validateNonBlank(symbol, "symbol").toUpperCase(Locale.ROOT);
        this.buyOrderId = validateNonBlank(buyOrderId, "buyOrderId");
        this.sellOrderId = validateNonBlank(sellOrderId, "sellOrderId");
        this.executedAt = Objects.requireNonNull(executedAt, "executedAt must not be null");

        if(price <= 0){
            throw new IllegalArgumentException("price must be greater than 0");
        }

        if(quantity <= 0){
            throw new IllegalArgumentException("quantity must be greater than 0");
        }

        this.price = price;
        this.quantity = quantity;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public long getPrice() {
        return price;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public long getQuantity() {
        return quantity;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    private static String validateNonBlank(String value, String fieldName) {
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + " must not be null or empty");
        }
        return value.trim();
    }

    @Override
    public java.lang.String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", buyOrderId='" + buyOrderId + '\'' +
                ", sellOrderId='" + sellOrderId + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", executedAt=" + executedAt +
                '}';
    }
}