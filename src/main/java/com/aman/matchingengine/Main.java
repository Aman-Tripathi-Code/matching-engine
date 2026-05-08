package com.aman.matchingengine;


import com.aman.matchingengine.cli.ScenarioFactory;
import com.aman.matchingengine.cli.ScenarioRunner;

public class Main {
    public static void main(String[] args) {
        ScenarioRunner runner = new ScenarioRunner();
        runner.runAll(ScenarioFactory.weekOneScenarios());
    }
}