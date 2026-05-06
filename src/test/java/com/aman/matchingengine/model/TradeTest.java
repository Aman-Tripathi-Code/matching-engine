package com.aman.matchingengine.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeTest {
    private static final Instant NOW = Instant.parse("2026-01-01T10:15:30Z");

    @Test
    void shouldCreateValidTrade() {
        Trade trade = new Trade(
                "TRD-1",
                " msft ",
                "BUY-1",
                "SELL-1",
                40125,
                50,
                NOW
        );

        assertEquals("TRD-1", trade.getTradeId());
        assertEquals("MSFT", trade.getSymbol());
        assertEquals("BUY-1", trade.getBuyOrderId());
        assertEquals("SELL-1", trade.getSellOrderId());
        assertEquals(40125, trade.getPrice());
        assertEquals(50, trade.getQuantity());
        assertEquals(NOW, trade.getExecutedAt());
    }

    @Test
    void shouldRejectNonPositivePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("TRD-1", "AAPL", "BUY-1", "SELL-1", 0, 10, NOW)
        );
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("TRD-1", "AAPL", "BUY-1", "SELL-1", 100, 0, NOW)
        );
    }

    @Test
    void shouldRejectBlankTradeId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Trade("  ", "AAPL", "BUY-1", "SELL-1", 100, 10, NOW)
        );
    }

}