package com.aman.matchingengine;


import com.aman.matchingengine.engine.MatchingEngine;
import com.aman.matchingengine.engine.OrderPlacementResult;
import com.aman.matchingengine.model.OrderSide;
import com.aman.matchingengine.model.Trade;

public class Main {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        //Seed AAPL book
        engine.placeOrder("AAPL-S1", "AAPL", OrderSide.SELL, 18550, 100);
        engine.placeOrder("AAPL-S2", "AAPL", OrderSide.SELL, 18600, 50);
        engine.placeOrder("AAPL-B1", "AAPL", OrderSide.BUY, 18450, 80);

        //Seed MSFT book
        engine.placeOrder("MSFT-B1", "MSFT", OrderSide.BUY, 40100, 70);
        engine.placeOrder("MSFT-S1", "MSFT", OrderSide.SELL, 40300, 60);

        System.out.println();
        System.out.println("=== BEFORE NEW ORDERS ===");
        System.out.println(engine.printOrderBook("AAPL"));
        System.out.println(engine.printOrderBook("MSFT"));

        OrderPlacementResult applResult = engine.placeOrder(
                "AAPL-B2",
                "AAPL",
                OrderSide.BUY,
                18600,
                120
        );

        System.out.println();
        System.out.println("=== TRADES GENERATED FOR APPL-B2 ===");
        for(Trade trade: applResult.getTrades()) {
            System.out.println(trade);
        }

        OrderPlacementResult msftResult = engine.placeOrder(
                "MSFT-S2",
                "MSFT",
                OrderSide.SELL,
                40050,
                50
        );

        System.out.println();
        System.out.println("=== TRADES GENERATED FOR MSFT-s2 ===");
        for(Trade trade: msftResult.getTrades()) {
            System.out.println(trade);
        }

        System.out.println();
        System.out.println("=== AFTER NEW ORDERS ===");
        System.out.println(engine.printOrderBook("AAPL"));
        System.out.println(engine.printOrderBook("MSFT"));

        System.out.println("Tracked symbols: " + engine.getTrackedSymbols());

    }
}