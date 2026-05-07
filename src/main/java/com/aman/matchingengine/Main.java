package com.aman.matchingengine;


import com.aman.matchingengine.engine.OrderBook;
import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.OrderSide;
import com.aman.matchingengine.model.Trade;

import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        OrderBook orderBook = new OrderBook("AAPL");

        orderBook.addOrder(new Order(
                "S-1",
                "AAPL",
                OrderSide.SELL,
                18550,
                100,
                1,
                Instant.now()
        ));

        orderBook.addOrder(new Order(
                "S-2",
                "AAPL",
                OrderSide.SELL,
                18600,
                50,
                2,
                Instant.now()
        ));

        orderBook.addOrder(new Order(
                "B-OLD",
                "AAPL",
                OrderSide.BUY,
                18400,
                80,
                3,
                Instant.now()
        ));

        System.out.println("BOOK BEFORE MATCHING");
        System.out.println(orderBook.printBook());

        Order incomingBuy = new Order(
                "B-1",
                "AAPL",
                OrderSide.BUY,
                18600,
                180,
                4,
                Instant.now()
        );

        List<Trade> trades = orderBook.processOrder(incomingBuy);

        System.out.println("TRADE GENERATED");
        for(Trade trade : trades) {
            System.out.println(trade);
        }

        System.out.println();
        System.out.println("BOOK AFTER MATCHING");
        System.out.println(orderBook.printBook());

    }
}