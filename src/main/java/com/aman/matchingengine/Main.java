package com.aman.matchingengine;


import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.OrderSide;
import com.aman.matchingengine.model.Trade;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        Order order = new Order(
                "ORD-1",
                "AAPL",
                OrderSide.BUY,
                18550,
                100,
                1,
                Instant.now()
        );

        Trade trade = new Trade(
                "TRD-1",
                "AAPL",
                "ORD-1",
                "ORD-2",
                18550,
                40,
                Instant.now()
        );
        System.out.println("Day 1 setup successful");
        System.out.println(order);
        System.out.println(trade);

    }
}