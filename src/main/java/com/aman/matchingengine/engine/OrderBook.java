package com.aman.matchingengine.engine;


import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.Trade;

import javax.swing.border.Border;
import java.util.*;

public class OrderBook {
    public static final Comparator<Order> BUY_PRIORITY = Comparator
            .comparingLong(Order::getPrice).reversed()
            .thenComparingLong(Order::getSequenceNumber)
            .thenComparing(Order::getOrderId);

    public static final Comparator<Order> SELL_PRIORITY = Comparator
            .comparingLong(Order::getPrice)
            .thenComparingLong(Order::getSequenceNumber)
            .thenComparing(Order::getOrderId);

    private final String symbol;
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final List<Trade> trades;

    public OrderBook(String symbol) {
        this.symbol = normalizeSymbol(symbol);
        this.buyOrders = new PriorityQueue<>(BUY_PRIORITY);
        this.sellOrders = new PriorityQueue<>(SELL_PRIORITY);
        this.trades = new ArrayList<>();
    }

    public void addOrder(Order order){
        Objects.requireNonNull((order),"order cannot be null");

        if(!symbol.equals(order.getSymbol())){
            throw new IllegalArgumentException("Order symbol " + order.getSymbol() + " does not belong to book " + symbol);
        }

        if(!order.isOpen()){
            throw new IllegalArgumentException("Only OPEN or PARTIALLY_FILLED orders can be added to the book");
        }

        if(order.isBuy()){
            buyOrders.offer(order);
        }else{
            sellOrders.offer(order);
        }
    }

    public void recordTrade(Trade trade){
        Objects.requireNonNull(trade,"trade cannot be null");


        if(!symbol.equals(trade.getSymbol())){
            throw new IllegalArgumentException("Trade symbol" + trade.getSymbol() + " does not belong to book " + symbol);
        }

        trades.add(trade);
    }

    public Order peekBestBid(){
        return buyOrders.peek();
    }

    public Order peekBestAsk(){
        return sellOrders.peek();
    }

    public int getBuyOrderCount(){
        return buyOrders.size();
    }

    public int getSellOrderCount(){
        return sellOrders.size();
    }

    public int getTotalOrderCount(){
        return buyOrders.size() + sellOrders.size();
    }

    public List<Order> getBuyOrdersSnapshot(){
        List<Order> snapshot = new ArrayList<>(buyOrders);
        snapshot.sort(BUY_PRIORITY);
        return List.copyOf(snapshot);
    }

    public List<Order> getSellOrdersSnapshot(){
        List<Order> snapshot = new ArrayList<>(sellOrders);
        snapshot.sort(SELL_PRIORITY);
        return List.copyOf(snapshot);
    }

    public List<Trade> getTrades() {
        return List.copyOf(trades);
    }

    public String printBook(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER BOOK: ").append(symbol).append(" ===\n");

        sb.append("BUY ORDERS (high price first)\n");
        List<Order> buySnapshot = getBuyOrdersSnapshot();
        if(buySnapshot.isEmpty()){
            sb.append("  [empty]\n");
        }else{
            for(Order order : buySnapshot){
                sb.append(" ").append(formatOrder(order)).append('\n');
            }
        }

        sb.append("SELL ORDERS (high price first)\n");
        List<Order> sellSnapshot = getSellOrdersSnapshot();
        if(sellSnapshot.isEmpty()){
            sb.append("  [empty]\n");
        }else{
            for(Order order : sellSnapshot){
                sb.append(" ").append(formatOrder(order)).append('\n');
            }
        }

        sb.append("TRADE RECORDED: ").append(trades.size()).append("\n");
        return sb.toString();
    }

    private String formatOrder(Order order){
        return "id = " + order.getOrderId()
                + ", symbol = " + order.getSymbol()
                + ", price = " + order.getPrice()
                + ", qty = " + order.getQuantity()
                + ", remaining = " + order.getRemainingQuantity()
                + ", sequenceNumber = " + order.getSequenceNumber()
                + ", status = " + order.getStatus();
    }

    private static String normalizeSymbol(String symbol){
        if(symbol == null || symbol.isBlank()){
            throw new IllegalArgumentException("symbol must not be blank");
        }else{
            return symbol.trim().toUpperCase(Locale.ROOT);
        }
    }



}