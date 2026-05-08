package com.aman.matchingengine.engine;

import com.aman.matchingengine.model.Order;

import com.aman.matchingengine.model.OrderSide;

import com.aman.matchingengine.model.OrderStatus;

import com.aman.matchingengine.model.Trade;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingEngineTest {

    @Test

    void shouldManageSeparateBooksPerSymbol() {

        MatchingEngine engine = new MatchingEngine();

        engine.placeOrder("AAPL-S1", "AAPL", OrderSide.SELL, 10000, 50);

        engine.placeOrder("MSFT-S1", "MSFT", OrderSide.SELL, 20000, 60);

        OrderPlacementResult aaplBuy = engine.placeOrder("AAPL-B1", "AAPL", OrderSide.BUY, 10100, 20);

        assertEquals(1, aaplBuy.getTrades().size());

        Trade aaplTrade = aaplBuy.getTrades().get(0);

        assertEquals("AAPL-B1", aaplTrade.getBuyOrderId());

        assertEquals("AAPL-S1", aaplTrade.getSellOrderId());

        assertEquals(10000, aaplTrade.getPrice());

        assertEquals(20, aaplTrade.getQuantity());

        assertEquals(1, engine.getTrades("AAPL").size());

        assertEquals(0, engine.getTrades("MSFT").size());

        assertEquals(30, engine.getSellOrders("AAPL").get(0).getRemainingQuantity());

        assertEquals(60, engine.getSellOrders("MSFT").get(0).getRemainingQuantity());

    }

    @Test

    void shouldAssignIncreasingSequenceNumbersAcrossSymbols() {

        MatchingEngine engine = new MatchingEngine();

        OrderPlacementResult r1 = engine.placeOrder("AAPL-B1", "AAPL", OrderSide.BUY, 10000, 10);

        OrderPlacementResult r2 = engine.placeOrder("MSFT-S1", "MSFT", OrderSide.SELL, 20000, 20);

        OrderPlacementResult r3 = engine.placeOrder("AAPL-S1", "AAPL", OrderSide.SELL, 10100, 30);

        assertEquals(1, r1.getOrder().getSequenceNumber());

        assertEquals(2, r2.getOrder().getSequenceNumber());

        assertEquals(3, r3.getOrder().getSequenceNumber());

    }

    @Test

    void shouldAutoGenerateOrderIdWhenMissing() {

        MatchingEngine engine = new MatchingEngine();

        OrderPlacementResult result = engine.placeOrder("AAPL", OrderSide.BUY, 10000, 10);

        assertTrue(result.getOrder().getOrderId().startsWith("ORD-AAPL-"));

        assertEquals(OrderStatus.OPEN, result.getOrder().getStatus());

    }

    @Test

    void shouldReturnSortedSnapshotsForKnownSymbol() {

        MatchingEngine engine = new MatchingEngine();

        engine.placeOrder("B-1", "AAPL", OrderSide.BUY, 10000, 10);

        engine.placeOrder("B-2", "AAPL", OrderSide.BUY, 10200, 10);

        engine.placeOrder("B-3", "AAPL", OrderSide.BUY, 10100, 10);

        List<Order> buyOrders = engine.getBuyOrders("AAPL");

        assertEquals(List.of("B-2", "B-3", "B-1"),

                buyOrders.stream().map(Order::getOrderId).toList());

    }

    @Test

    void shouldReturnEmptyViewsForUnknownSymbol() {

        MatchingEngine engine = new MatchingEngine();

        assertTrue(engine.getBuyOrders("GOOG").isEmpty());

        assertTrue(engine.getSellOrders("GOOG").isEmpty());

        assertTrue(engine.getTrades("GOOG").isEmpty());

        String printedBook = engine.printOrderBook("GOOG");

        assertTrue(printedBook.contains("ORDER BOOK: GOOG"));

    }

    @Test

    void shouldTrackSymbolsThatHaveBooks() {

        MatchingEngine engine = new MatchingEngine();

        engine.placeOrder("AAPL", OrderSide.BUY, 10000, 10);

        engine.placeOrder("MSFT", OrderSide.SELL, 20000, 20);

        assertEquals(2, engine.getTrackedSymbolCount());

        assertEquals(List.of("AAPL", "MSFT"), engine.getTrackedSymbols().stream().toList());

    }

}