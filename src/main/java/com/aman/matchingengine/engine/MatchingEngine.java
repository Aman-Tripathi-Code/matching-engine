package com.aman.matchingengine.engine;


import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.OrderSide;
import com.aman.matchingengine.model.Trade;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks;
    private final AtomicLong sequenceGenerator;
    private  final AtomicLong orderIdGenerator;

    public MatchingEngine() {
        this.orderBooks = new HashMap<>();
        this.sequenceGenerator = new AtomicLong(1);
        this.orderIdGenerator = new AtomicLong(1);
    }

    public OrderPlacementResult placeOrder(String symbol, OrderSide side, long price, long quantity) {
        return placeOrder(null, symbol, side, price, quantity);
    }

    public OrderPlacementResult placeOrder(
            String orderId,
            String symbol,
            OrderSide side,
            long price,
            long quantity
    ) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String finalOrderId = resolveOrderId(orderId, normalizedSymbol);

        Order order = new Order(
                finalOrderId,
                normalizedSymbol,
                Objects.requireNonNull(side, "side must not be null"),
                price,
                quantity,
                sequenceGenerator.getAndIncrement(),
                Instant.now()
        );

        OrderBook orderBook = orderBooks.computeIfAbsent(normalizedSymbol, OrderBook::new);

        logOrderReceived(order);

        List<Trade> generatedTrades = orderBook.processOrder(order);

        if (generatedTrades.isEmpty()) {
            logOrderResting(order);
        } else {
            for (Trade trade : generatedTrades) {
                logTrade(trade);
            }
            logOrderPostMatch(order);
        }
        return new OrderPlacementResult(order, generatedTrades);
    }

    public Optional<OrderBook> getOrderBook(String symbol) {
        return Optional.ofNullable(orderBooks.get(normalizeSymbol(symbol)));
    }

    public List<Order> getBuyOrders(String symbol) {
        OrderBook orderBook = orderBooks.get(normalizeSymbol(symbol));
        return orderBook == null ? List.of() : orderBook.getBuyOrdersSnapshot();
    }

    public List<Order> getSellOrders(String symbol){
        OrderBook orderBook = orderBooks.get(normalizeSymbol(symbol));
        return orderBook == null ? List.of() : orderBook.getSellOrdersSnapshot();
    }

    public List<Trade> getTrades(String symbol) {
        OrderBook orderBook = orderBooks.get(normalizeSymbol(symbol));
        return orderBook == null ? List.of() : orderBook.getTrades();
    }

    public String printOrderBook(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        OrderBook orderBook = orderBooks.get(normalizedSymbol);

        if(orderBook == null) {
            return new OrderBook(normalizedSymbol).printBook();
        }
        return orderBook.printBook();
    }

    public Set<String> getTrackedSymbols() {
        return Collections.unmodifiableSet(new TreeSet<>(orderBooks.keySet()));
    }

    public int getTrackedSymbolCount() {
        return orderBooks.size();
    }

    private String resolveOrderId(String orderId, String symbol) {
        if(orderId != null && !orderId.isBlank()) {
            return orderId.trim();
        }
        return "ORD-" + symbol + "-" + orderIdGenerator.getAndIncrement();
    }

    private static String normalizeSymbol(String symbol) {
        if(symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    private void logOrderReceived(Order order) {
        System.out.printf(
                "[ENGINE] RECEIVED orderId=%s symbol=%s side=%s price=%d qty=%d seq=%d%n",
                order.getOrderId(),
                order.getSymbol(),
                order.getSide(),
                order.getPrice(),
                order.getQuantity(),
                order.getSequenceNumber()
        );
    }

    private void logOrderResting(Order order) {
        System.out.printf(
                "[ENGINE] RESTING orderId=%s symbol=%s remaining=%d status=%s%n",
                order.getOrderId(),
                order.getSymbol(),
                order.getRemainingQuantity(),
                order.getStatus()
        );
    }

    private void logTrade(Trade trade) {
        System.out.printf(
                "[ENGINE] TRADE tradeId=%s symbol=%s buyOrderId=%s sellOrderId=%s price=%d qty=%d%n",
                trade.getTradeId(),
                trade.getSymbol(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getPrice(),
                trade.getQuantity()
        );
    }

    private void logOrderPostMatch(Order order) {
        System.out.printf(
                "[ENGINE] POST_MATCH orderId=%s symbol=%s remaining=%d status=%s%n",
                order.getOrderId(),
                order.getSymbol(),
                order.getRemainingQuantity(),
                order.getStatus()
        );
    }

}