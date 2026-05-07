package com.aman.matchingengine.engine;

import com.aman.matchingengine.model.Order;

import com.aman.matchingengine.model.OrderSide;

import com.aman.matchingengine.model.OrderStatus;

import com.aman.matchingengine.model.Trade;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderBookTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:15:30Z");

    @Test

    void shouldPrioritizeHigherBuyPriceFirst() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 1));

        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10200, 10, 2));

        book.addOrder(order("B-3", "AAPL", OrderSide.BUY, 10100, 10, 3));

        assertEquals("B-2", book.peekBestBid().getOrderId());

    }

    @Test

    void shouldPrioritizeLowerSellPriceFirst() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10300, 10, 1));

        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10100, 10, 2));

        book.addOrder(order("S-3", "AAPL", OrderSide.SELL, 10200, 10, 3));

        assertEquals("S-2", book.peekBestAsk().getOrderId());

    }

    @Test

    void shouldUseSequenceAsTieBreakerWhenBuyPricesAreSame() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10000, 10, 2));

        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 1));

        assertEquals("B-1", book.peekBestBid().getOrderId());

    }

    @Test

    void shouldUseSequenceAsTieBreakerWhenSellPricesAreSame() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10000, 10, 2));

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10000, 10, 1));

        assertEquals("S-1", book.peekBestAsk().getOrderId());

    }

    @Test

    void shouldFullyMatchIncomingBuyAgainstSingleRestingSell() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10000, 100, 1));

        Order incomingBuy = order("B-1", "AAPL", OrderSide.BUY, 10100, 100, 2);

        List<Trade> trades = book.processOrder(incomingBuy);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals("B-1", trade.getBuyOrderId());

        assertEquals("S-1", trade.getSellOrderId());

        assertEquals(10000, trade.getPrice()); // resting order price

        assertEquals(100, trade.getQuantity());

        assertEquals(OrderStatus.FILLED, incomingBuy.getStatus());

        assertEquals(0, incomingBuy.getRemainingQuantity());

        assertEquals(0, book.getBuyOrderCount());

        assertEquals(0, book.getSellOrderCount());

        assertEquals(1, book.getTrades().size());

    }

    @Test

    void shouldPartiallyFillIncomingBuyAndLeaveRemainderOnBook() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10000, 40, 1));

        Order incomingBuy = order("B-1", "AAPL", OrderSide.BUY, 10100, 100, 2);

        List<Trade> trades = book.processOrder(incomingBuy);

        assertEquals(1, trades.size());

        assertEquals(40, trades.get(0).getQuantity());

        assertEquals(10000, trades.get(0).getPrice());

        assertEquals(OrderStatus.PARTIALLY_FILLED, incomingBuy.getStatus());

        assertEquals(60, incomingBuy.getRemainingQuantity());

        assertEquals(1, book.getBuyOrderCount());

        assertEquals(0, book.getSellOrderCount());

        assertEquals("B-1", book.peekBestBid().getOrderId());

        assertEquals(60, book.peekBestBid().getRemainingQuantity());

    }

    @Test

    void shouldMatchIncomingBuyAcrossMultipleSellOrdersInPriceTimePriority() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10000, 40, 1));

        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10000, 30, 2));

        book.addOrder(order("S-3", "AAPL", OrderSide.SELL, 10100, 50, 3));

        Order incomingBuy = order("B-1", "AAPL", OrderSide.BUY, 10100, 100, 4);

        List<Trade> trades = book.processOrder(incomingBuy);

        assertEquals(3, trades.size());

        assertEquals(List.of("S-1", "S-2", "S-3"),

                trades.stream().map(Trade::getSellOrderId).toList());

        assertEquals(List.of(10000L, 10000L, 10100L),

                trades.stream().map(Trade::getPrice).toList());

        assertEquals(List.of(40L, 30L, 30L),

                trades.stream().map(Trade::getQuantity).toList());

        assertEquals(OrderStatus.FILLED, incomingBuy.getStatus());

        assertEquals(0, incomingBuy.getRemainingQuantity());

        assertEquals(0, book.getBuyOrderCount());

        assertEquals(1, book.getSellOrderCount());

        assertEquals("S-3", book.peekBestAsk().getOrderId());

        assertEquals(20, book.peekBestAsk().getRemainingQuantity());

    }

    @Test

    void shouldNotMatchWhenPricesDoNotCross() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10200, 50, 1));

        Order incomingBuy = order("B-1", "AAPL", OrderSide.BUY, 10100, 50, 2);

        List<Trade> trades = book.processOrder(incomingBuy);

        assertEquals(0, trades.size());

        assertEquals(OrderStatus.OPEN, incomingBuy.getStatus());

        assertEquals(50, incomingBuy.getRemainingQuantity());

        assertEquals(1, book.getBuyOrderCount());

        assertEquals(1, book.getSellOrderCount());

        assertEquals("B-1", book.peekBestBid().getOrderId());

        assertEquals("S-1", book.peekBestAsk().getOrderId());

    }

    @Test

    void shouldMatchIncomingSellAgainstBestBuysInPriceTimePriority() {

        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10100, 50, 1));

        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10100, 60, 2));

        book.addOrder(order("B-3", "AAPL", OrderSide.BUY, 10050, 30, 3));

        Order incomingSell = order("S-1", "AAPL", OrderSide.SELL, 10000, 70, 4);

        List<Trade> trades = book.processOrder(incomingSell);

        assertEquals(2, trades.size());

        assertEquals(List.of("B-1", "B-2"),

                trades.stream().map(Trade::getBuyOrderId).toList());

        assertEquals(List.of(10100L, 10100L),

                trades.stream().map(Trade::getPrice).toList());

        assertEquals(List.of(50L, 20L),

                trades.stream().map(Trade::getQuantity).toList());

        assertEquals(OrderStatus.FILLED, incomingSell.getStatus());

        assertEquals(0, incomingSell.getRemainingQuantity());

        assertEquals(2, book.getBuyOrderCount());

        assertEquals(0, book.getSellOrderCount());

        assertEquals("B-2", book.peekBestBid().getOrderId());

        assertEquals(40, book.peekBestBid().getRemainingQuantity());

    }

    @Test

    void shouldRejectOrderFromDifferentSymbol() {

        OrderBook book = new OrderBook("AAPL");

        IllegalArgumentException ex = assertThrows(

                IllegalArgumentException.class,

                () -> book.addOrder(order("B-1", "MSFT", OrderSide.BUY, 10000, 10, 1))

        );

        assertEquals("Order symbol MSFT does not belong to book AAPL", ex.getMessage());

    }

    @Test

    void shouldRejectClosedOrder() {

        OrderBook book = new OrderBook("AAPL");

        Order order = order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 1);

        order.fill(10);

        IllegalArgumentException ex = assertThrows(

                IllegalArgumentException.class,

                () -> book.addOrder(order)

        );

        assertEquals("Only OPEN or PARTIALLY_FILLED orders can be added to the book", ex.getMessage());

    }

    private Order order(

            String orderId,

            String symbol,

            OrderSide side,

            long price,

            long quantity,

            long sequenceNumber

    ) {

        return new Order(orderId, symbol, side, price, quantity, sequenceNumber, NOW);

    }

}