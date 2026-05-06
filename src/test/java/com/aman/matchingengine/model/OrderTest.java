package com.aman.matchingengine.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:15:30Z");

    @Test
    void shouldCreateValidOrder() {
        Order order = new Order(
                "ORD-1",
                " aapl ",
                OrderSide.BUY,
                18550,
                100,
                1,
                NOW
        );
        assertEquals("ORD-1", order.getOrderId());
        assertEquals("AAPL", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(18550, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(100, order.getRemainingQuantity());
        assertEquals(0, order.getFilledQuantity());
        assertEquals(1, order.getSequenceNumber());
        assertEquals(NOW, order.getCreatedAt());
        assertEquals(OrderStatus.OPEN, order.getStatus());
        assertTrue(order.isBuy());
        assertTrue(order.isOpen());
    }

    @Test
    void shouldRejectBlankOrderId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Order("", "AAPL", OrderSide.BUY, 18550, 100, 1, NOW)
        );
    }

    @Test
    void shouldRejectBlankSymbol() {
        assertThrows(IllegalArgumentException.class, () ->
                new Order("ORD-1", "   ", OrderSide.BUY, 18550, 100, 1, NOW)
        );
    }

    @Test
    void shouldRejectNonPositivePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new Order("ORD-1", "AAPL", OrderSide.BUY, 0, 100, 1, NOW)
        );
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Order("ORD-1", "AAPL", OrderSide.BUY, 18550, 0, 1, NOW)
        );
    }

    @Test
    void shouldPartiallyFillOrder() {
        Order order = new Order("ORD-1", "AAPL", OrderSide.BUY, 18550, 100, 1, NOW);
        order.fill(40);
        assertEquals(60, order.getRemainingQuantity());
        assertEquals(40, order.getFilledQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
        assertTrue(order.isOpen());
    }

    @Test
    void shouldFullyFillOrder() {
        Order order = new Order("ORD-1", "AAPL", OrderSide.SELL, 18550, 100, 2, NOW);
        order.fill(100);
        assertEquals(0, order.getRemainingQuantity());
        assertEquals(100, order.getFilledQuantity());
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertTrue(order.isFilled());
    }

    @Test
    void shouldRejectOverfill() {
        Order order = new Order("ORD-1", "AAPL", OrderSide.BUY, 18550, 100, 1, NOW);
        assertThrows(IllegalArgumentException.class, () -> order.fill(101));
    }

}