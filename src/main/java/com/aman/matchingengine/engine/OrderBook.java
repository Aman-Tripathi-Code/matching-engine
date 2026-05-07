package com.aman.matchingengine.engine;


import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.Trade;

import java.time.Instant;
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

    private long nextTradeSequence = 1L;

    public OrderBook(String symbol) {
        this.symbol = normalizeSymbol(symbol);
        this.buyOrders = new PriorityQueue<>(BUY_PRIORITY);
        this.sellOrders = new PriorityQueue<>(SELL_PRIORITY);
        this.trades = new ArrayList<>();
    }
    public void validateOrderForThisBook(Order order){
        Objects.requireNonNull((order),"order cannot be null");

        if(!symbol.equals(order.getSymbol())){
            throw new IllegalArgumentException("Order symbol " + order.getSymbol() + " does not belong to book " + symbol);
        }

        if(!order.isOpen()){
            throw new IllegalArgumentException("Only OPEN or PARTIALLY_FILLED orders can be added to the book");
        }
    }

    public void addOrder(Order order){

        validateOrderForThisBook(order);

        if(order.isBuy()){
            buyOrders.offer(order);
        }else{
            sellOrders.offer(order);
        }
    }


    public List<Trade> processOrder(Order incomingOrder){
        validateOrderForThisBook(incomingOrder);

        List<Trade> generatedTrades = new ArrayList<>();

        if(incomingOrder.isBuy()){
            matchIncomingBuy(incomingOrder,generatedTrades);
        }else{
            matchIncomingSell(incomingOrder,generatedTrades);
        }

        if(incomingOrder.getRemainingQuantity() > 0){
            addOrder(incomingOrder);
        }
        return List.copyOf(generatedTrades);
    }

    private void matchIncomingBuy(Order incomingBuy, List<Trade> generatedTrades){
        while(incomingBuy.getRemainingQuantity() > 0 && !sellOrders.isEmpty()) {
            Order restingSell = sellOrders.peek();

            //No price cross - stop
            if(incomingBuy.getPrice() < restingSell.getPrice()){
                break;
            }

            long executedQuantity = Math.min(incomingBuy.getRemainingQuantity(), restingSell.getRemainingQuantity());

            long executionPrice = restingSell.getPrice();

            restingSell.fill(executedQuantity);
            incomingBuy.fill(executedQuantity);

            Trade trade = createTrade(incomingBuy, restingSell, executionPrice, executedQuantity);
            trades.add(trade);

            generatedTrades.add(trade);

            if(restingSell.isFilled()){
                sellOrders.poll();
            }
        }
    }

    private void matchIncomingSell(Order incomingSell, List<Trade> generatedTrades){
        while(incomingSell.getRemainingQuantity() > 0 && !buyOrders.isEmpty()) {
            Order restingBuy = buyOrders.peek();

            //No price cross - stop
            if(incomingSell.getPrice() > restingBuy.getPrice()){
                break;
            }

            long executedQuantity = Math.min(incomingSell.getRemainingQuantity(), restingBuy.getRemainingQuantity());

            long executionPrice = restingBuy.getPrice();

            restingBuy.fill(executedQuantity);
            incomingSell.fill(executedQuantity);

            Trade trade = createTrade(restingBuy, incomingSell, executionPrice, executedQuantity);
            trades.add(trade);

            generatedTrades.add(trade);

            if(restingBuy.isFilled()){
                buyOrders.poll();
            }
        }
    }


    private Trade createTrade(Order buyOrder, Order sellOrder, long executionPrice, long executedQuantity){
        String tradeId = "TRD-" + symbol + "-" + nextTradeSequence++;
        return new Trade(tradeId,symbol,buyOrder.getOrderId(), sellOrder.getOrderId(), executionPrice,executedQuantity,Instant.now());
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

    public String getSymbol(){
        return symbol;
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

        sb.append("TRADES\n");
        if(trades.isEmpty()){
            sb.append("  [empty]\n");
        }else{
            for(Trade trade : trades){
                sb.append(" ").append(formatTrade(trade)).append('\n');
            }
        }

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


    private String formatTrade(Trade trade){
        return "tradeId = " + trade.getTradeId()
                + ", symbol = " + trade.getSymbol()
                + ", buyOrderId = " + trade.getBuyOrderId()
                + ", sellOrderId = " + trade.getSellOrderId()
                + ", price = " + trade.getPrice()
                + ", qty = " + trade.getQuantity();
    }

    private static String normalizeSymbol(String symbol){
        if(symbol == null || symbol.isBlank()){
            throw new IllegalArgumentException("symbol must not be blank");
        }else{
            return symbol.trim().toUpperCase(Locale.ROOT);
        }
    }



}