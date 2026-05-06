package com.aman.matchingengine.model;


import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public class Order {
    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final long price;
    private final long quantity;
    private long remainingQuantity; //mutable because fills happen over time
    private final long sequenceNumber;
    private final Instant createdAt;
    private OrderStatus status;


    public Order(
            String orderId,
            String symbol,
            OrderSide side,
            long price,
            long quantity,
            long sequenceNumber,
            Instant createdAt){
        this.orderId = validateNonBlank(orderId, "orderId");
        this.symbol = validateNonBlank(symbol, "symbol").toUpperCase(Locale.ROOT);
        this.side = Objects.requireNonNull(side, "side must not be null");

        if(price <= 0){
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if(sequenceNumber < 0){
            throw new IllegalArgumentException("Sequence number must be greater than equal to 0");
        }

        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = Objects.requireNonNull(createdAt, "createAt must not be null");
        this.status = OrderStatus.OPEN;
    }

    public void fill(long fillQuantity){
        if(fillQuantity <= 0){
            throw new IllegalArgumentException("Fill quantity must be greater than 0");
        }
        if(status == OrderStatus.FILLED){
            throw new IllegalArgumentException("Fill quantity is already filled");
        }
        if(status == OrderStatus.REJECTED){
            throw new IllegalArgumentException("Fill quantity is rejected");
        }
        if(fillQuantity > remainingQuantity){
            throw new IllegalArgumentException("Fill quantity is greater than remaining quantity");
        }

        remainingQuantity -= fillQuantity;

        if(remainingQuantity == 0){
            status = OrderStatus.FILLED;
        }else{
            status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public long getFilledQuantity(){
        return quantity - remainingQuantity;
    }

    public boolean isBuy(){
        return side == OrderSide.BUY;
    }

    public boolean isSell(){return side == OrderSide.SELL;}

    public boolean isFilled(){
        return remainingQuantity == 0;
    }

    public boolean isOpen(){
        return status == OrderStatus.OPEN || status == OrderStatus.PARTIALLY_FILLED;
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

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    private static String validateNonBlank(String value, String fieldName) {
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + " must not be null or empty");
        }
        return value.trim();
    }

    @Override
    public java.lang.String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", quantity=" + quantity +
                ", remainingQuantity=" + remainingQuantity +
                ", sequenceNumber=" + sequenceNumber +
                ", createAt=" + createdAt +
                ", status=" + status +
                '}';
    }
}