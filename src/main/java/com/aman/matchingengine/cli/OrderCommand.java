package com.aman.matchingengine.cli;


import com.aman.matchingengine.model.OrderSide;

import java.util.Locale;
import java.util.Objects;

public final class OrderCommand {
    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final long price;
    private final long quantity;
    private final String note;

    public OrderCommand(
            String orderId,
            String symbol,
            OrderSide side,
            long price,
            long quantity,
            String note
    ) {
        this.orderId = validateNonBlank(orderId, "orderId");
        this.symbol = validateNonBlank(symbol, "symbol").toUpperCase(Locale.ROOT);
        this.side = Objects.requireNonNull(side, "side must not be null");

        if(price < 0){
            throw new IllegalArgumentException("price must be greater than zero");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        this.price = price;
        this.quantity = quantity;
        this.note = note == null ? "" : note;
    }

    public static OrderCommand buy(
            String orderId,
            String symbol,
            long price,
            long quantity,
            String note
    ){
        return new OrderCommand(orderId, symbol, OrderSide.BUY, price, quantity, note);
    }

    public static OrderCommand sell(
            String orderId,
            String symbol,
            long price,
            long quantity,
            String note
    ){
        return new OrderCommand(orderId, symbol, OrderSide.SELL, price, quantity, note);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }

    public boolean hasNote(){
        return !note.isBlank();
    }

    public String format(){
        return "orderId=" + orderId
                + ", symbol=" + symbol
                + ", side=" + side
                + ", price=" + price
                + ", quantity=" + quantity;
    }

    private static String validateNonBlank(String value, String fieldName) {
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + "must not be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "OrderCommand{" +
                "orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side=" + side + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", note=" + note + '\'' +
                '}';
    }
}