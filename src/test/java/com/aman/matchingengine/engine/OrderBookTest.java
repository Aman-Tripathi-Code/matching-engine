package com.aman.matchingengine.engine;

import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.OrderSide;
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
    void shouldPrioritizeEarlierBuySequenceWhenPricesAreSame() {
        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10000, 10, 2));
        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 1));

        assertEquals("B-1", book.peekBestBid().getOrderId());
    }

    @Test
    void shouldPrioritizeEarlierSellSequenceWhenPricesAreSame() {
        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10000, 10, 2));
        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10000, 10, 1));

        assertEquals("S-1", book.peekBestAsk().getOrderId());
    }

    @Test
    void shouldReturnSortedBuySnapshot() {
        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 3));
        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10100, 10, 2));
        book.addOrder(order("B-3", "AAPL", OrderSide.BUY, 10100, 10, 1));

        List<String> orderIds = book.getBuyOrdersSnapshot()
                .stream()
                .map(Order::getOrderId)
                .toList();

        assertEquals(List.of("B-3", "B-2", "B-1"), orderIds);
    }

    @Test
    void shouldReturnSortedSellSnapshot() {
        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10200, 10, 3));
        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10100, 10, 2));
        book.addOrder(order("S-3", "AAPL", OrderSide.SELL, 10100, 10, 1));

        List<String> orderIds = book.getSellOrdersSnapshot()
                .stream()
                .map(Order::getOrderId)
                .toList();

        assertEquals(List.of("S-3", "S-2", "S-1"), orderIds);
    }

    @Test
    void shouldMaintainSeparateBuyAndSellBooks() {
        OrderBook book = new OrderBook("AAPL");

        book.addOrder(order("B-1", "AAPL", OrderSide.BUY, 10000, 10, 1));
        book.addOrder(order("B-2", "AAPL", OrderSide.BUY, 10100, 10, 2));
        book.addOrder(order("S-1", "AAPL", OrderSide.SELL, 10500, 10, 3));
        book.addOrder(order("S-2", "AAPL", OrderSide.SELL, 10400, 10, 4));

        assertEquals(2, book.getBuyOrderCount());
        assertEquals(2, book.getSellOrderCount());
        assertEquals(4, book.getTotalOrderCount());
        assertEquals("B-2", book.peekBestBid().getOrderId());
        assertEquals("S-2", book.peekBestAsk().getOrderId());
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