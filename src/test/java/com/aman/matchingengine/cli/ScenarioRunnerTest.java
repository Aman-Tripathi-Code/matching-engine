package com.aman.matchingengine.cli;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioRunnerTest {

    @Test
    void shouldCreateWeekOneScenarios() {
        List<Scenario> scenarios = ScenarioFactory.weekOneScenarios();
        assertEquals(6, scenarios.size());
        for (Scenario scenario : scenarios) {
            assertFalse(scenario.getName().isBlank());
            assertFalse(scenario.getDescription().isBlank());
            assertFalse(scenario.getCommands().isEmpty());
        }
    }

    @Test
    void shouldRunAllScenariosAndPrintUsefulOutput() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ScenarioRunner runner = new ScenarioRunner(new PrintStream(bytes));
        runner.runAll(ScenarioFactory.weekOneScenarios());
        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Day 5 CLI Scenario Demo"));
        assertTrue(output.contains("NO CROSS"));
        assertTrue(output.contains("FULL MATCH"));
        assertTrue(output.contains("PARTIAL FILL"));
        assertTrue(output.contains("PRICE-TIME PRIORITY"));
        assertTrue(output.contains("MULTI-SYMBOL ISOLATION"));
        assertTrue(output.contains("FINAL BOOK SNAPSHOTS"));
        assertTrue(output.contains("SCENARIO COMPLETE"));
    }
}