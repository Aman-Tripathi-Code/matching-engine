package com.aman.matchingengine.cli;


import java.util.List;
import java.util.Objects;

public final class Scenario {
    private final String name;
    private final String description;
    private final List<OrderCommand> commands;


    public Scenario(final String name, final String description, List<OrderCommand> commands) {
        this.name = validateNonBlank(name, "name");
        this.description = validateNonBlank(description, "description");
        this.commands = List.copyOf(Objects.requireNonNull(commands, "commands must not be null"));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<OrderCommand> getCommands() {
        return commands;
    }

    private static String validateNonBlank(String value, String fieldName){
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + "must not be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "name=" + name +
                ", description=" + description +
                ", commands=" + commands +
                "}";
    }
}