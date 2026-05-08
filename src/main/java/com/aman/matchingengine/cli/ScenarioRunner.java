package com.aman.matchingengine.cli;


import com.aman.matchingengine.engine.MatchingEngine;
import com.aman.matchingengine.engine.OrderPlacementResult;
import com.aman.matchingengine.model.Order;
import com.aman.matchingengine.model.Trade;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ScenarioRunner {
    private final PrintStream out;

    public ScenarioRunner(){
        this(System.out);
    }

    public ScenarioRunner(PrintStream out){
        this.out = Objects.requireNonNull(out, "out msut not be null");
    }

    public void runAll(List<Scenario> scenarios) {
        Objects.requireNonNull(scenarios, "scenarios msut not be null");

        if (scenarios.isEmpty()) {
            throw new IllegalArgumentException("scenarios msut not be empty");
        }

        printMainHeader();

        for (int i = 0; i < scenarios.size(); i++) {
            runScenario(i + 1, scenarios.get(i));
        }
    }

    private void runScenario(int scenarioNumber, Scenario scenario){
        MatchingEngine engine = new MatchingEngine(false);
        Set<String> touchedSymbols = new TreeSet<>();

        printScenarioHeader(scenarioNumber, scenario);

        int step = 1;

        for(OrderCommand command: scenario.getCommands()){
            touchedSymbols.add(command.getSymbol());

            out.printf("STEP %02d%n", step++);
            out.println("Input: " + command.format());

            if(command.hasNote()){
                out.println("Note : " + command.getNote());
            }


            OrderPlacementResult result = engine.placeOrder(
                    command.getOrderId(),
                    command.getSymbol(),
                    command.getSide(),
                    command.getPrice(),
                    command.getQuantity()
            );

            printOrderResult(result);
            out.println();
        }

        touchedSymbols.addAll(engine.getTrackedSymbols());

        out.println("FINAL BOOK SNAPSHOTS");
        out.println("--------------------");

        for(String symbol: touchedSymbols){
            out.println(engine.printOrderBook(symbol));
        }

        out.println("SCENARIO COMPLETE");
        out.println();
    }

    private void printMainHeader(){
        out.println("===========================================================");
        out.println("Day 5 CLI Scenario Demo");
        out.println("Distributed Stock Order Matching Engine - Week 1");
        out.println("===========================================================");
        out.println();
    }

    private void printScenarioHeader(int scenarioNumber, Scenario scenario){
        out.println("===========================================================");
        out.printf("SCENARIO %d: %s%n", scenarioNumber, scenario.getName());
        out.println("===========================================================");
        out.println(scenario.getDescription());
        out.println();
    }

    private void printOrderResult(OrderPlacementResult result){
        Order order = result.getOrder();

        out.printf(
                "Result: orderId=%s status=%s filled=%d remaining=%d%n",
                order.getOrderId(),
                order.getStatus(),
                order.getFilledQuantity(),
                order.getRemainingQuantity()
        );

        if(!result.hasTrades()){
            out.println("Trades: none");
            return;
        }

        out.println("Trades:");

        for(Trade trade: result.getTrades()){
            out.printf(
                    " tradeId=%s symbol=%s buyOrderId=%s sellOrderId=%s price=%d quantity=%d%n",
                    trade.getTradeId(),
                    trade.getSymbol(),
                    trade.getBuyOrderId(),
                    trade.getSellOrderId(),
                    trade.getPrice(),
                    trade.getQuantity()
            );
        }
    }
}