package com.aman.matchingengine;


import com.aman.matchingengine.engine.OrderBook;
import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.OrderSide;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        OrderBook orderBook = new OrderBook("AAPL");

        orderBook.addOrder(new Order(
            "B-1",
            "AAPL",
            OrderSide.BUY,
            18550,
            100,
            1,
            Instant.now()
        ));

        orderBook.addOrder(new Order(
                "B-2",
                "AAPL",
                OrderSide.BUY,
                18600,
                50,
                2,
                Instant.now()
        ));

        orderBook.addOrder(new Order(
                "S-1",
                "AAPL",
                OrderSide.SELL,
                18650,
                70,
                3,
                Instant.now()
        ));

        orderBook.addOrder(new Order(
                "S-2",
                "AAPL",
                OrderSide.SELL,
                18620,
                30,
                4,
                Instant.now()
        ));

        System.out.println("Best Bid: " + orderBook.peekBestBid());
        System.out.println("Best Ask: " + orderBook.peekBestAsk());
        System.out.println();
        System.out.println(orderBook.printBook());

    }
}