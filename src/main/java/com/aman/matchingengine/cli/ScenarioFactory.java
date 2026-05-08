package com.aman.matchingengine.cli;


import java.util.List;

public final class ScenarioFactory {
    private ScenarioFactory() {
    }

    public static List<Scenario> weekOneScenarios(){
        return List.of(
                noCrossScenario(),
                fullMatchScenario(),
                partialFillScenario(),
                priceTimePriorityScenario(),
                incomingSellMatchesBestBuyScenario(),
                mutliSymbolIsolationScanario()
        );
    }

    private static Scenario noCrossScenario() {
        return new Scenario(
                "NO CROSS - orders rest on book",
                "A buy below the best ask should not execute. Both orders should remain in the book.",
                List.of(
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10000,
                                10,
                                "Buy order enters at 100.00"
                        ),
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10100,
                                5,
                                "Sell order enters at 101.00, so prices do not cross"
                        )
                )
        );
    }

    private static Scenario fullMatchScenario(){
        return new Scenario(
                "FULL MATCH - incoming buy consumes resting sell",
                "A buy order with price >= best ask should execute against the resting sell",
                List.of(
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10000,
                                50,
                                "Resting sell at 100.00"
                        ),
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10100,
                                50,
                                "Incoming buy crosses the ask and fully matches"
                        )
                )
        );
    }

    private static Scenario partialFillScenario(){
        return new Scenario(
                "PARTIAL FILL - incoming buy has leftover quantity",
                "The incoming buy is larger than available sell quantity, so leftover buy quantity should rest",
                List.of(
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10000,
                                40,
                                "Only 40 shares available on sell side"
                        ),
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10100,
                                100,
                                "Incoming buy wants 100, matches 40, leaves 60 resting"
                        )
                )
        );
    }

    private static Scenario priceTimePriorityScenario(){
        return new Scenario(
                "PRICE-TIME PRIORITY - same price uses earlier order first",
                "Multiple sell orders exist. The engine should match lower price first and, for same price, earlier sequence first",
                List.of(
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10000,
                                40,
                                "First sell at 100.00"
                        ),
                        OrderCommand.sell(
                                "AAPL-S2",
                                "AAPL",
                                10000,
                                30,
                                "Second sell at same price, Is should match after AAPL-S1"
                        ),
                        OrderCommand.sell(
                                "AAPL-S3",
                                "AAPL",
                                10100,
                                50,
                                "Higher ask. It should match only after both 100.00 sells"
                        ),
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10100,
                                100,
                                "Incoming buy should match S1, then S2, then part of S3"
                        )
                )
        );
    }


    private static Scenario incomingSellMatchesBestBuyScenario(){
        return new Scenario(
                "SELL MATCHING - incoming sell hits best bids",
                "An incoming sell should match the highest buy price first. If price tie, earlier buy sequence wins.",
                List.of(
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10100,
                                50,
                                "First buy at 101.00"
                        ),
                        OrderCommand.buy(
                                "AAPL-B2",
                                "AAPL",
                                10100,
                                60,
                                "Second buy at same price. It should match after AAPL-B1"
                        ),
                        OrderCommand.buy(
                                "AAPL-B3",
                                "AAPL",
                                10050,
                                30,
                                "Lower bid. It should be lower priority"
                        ),
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10000,
                                70,
                                "Incoming sell should match B1 fully and B2 partially"
                        )
                )
        );
    }


    private static Scenario mutliSymbolIsolationScanario(){
        return new Scenario(
                "MULTI-SYMBOL ISOLATION - AAPL and MSFT books are separate",
                "AAPL orders should never match against MSFT orders. Each symbol has its own independent order book.",
                List.of(
                        OrderCommand.sell(
                                "AAPL-S1",
                                "AAPL",
                                10000,
                                50,
                                "AAPL resting sell"
                        ),
                        OrderCommand.sell(
                                "MSFT-S1",
                                "MSFT",
                                20000,
                                60,
                                "MSFT resting sell"
                        ),
                        OrderCommand.buy(
                                "AAPL-B1",
                                "AAPL",
                                10100,
                                20,
                                "This should match only against AAPL-S1, not MSFT-S1"
                        )
                )
        );
    }



}