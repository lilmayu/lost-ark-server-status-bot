package dev.mayuna.lostarkbot.console;

import dev.mayuna.lostarkbot.console.commands.basic.HelpConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.basic.StopConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class ConsoleCommandManager {

    // Data
    private static @Getter final List<AbstractConsoleCommand> consoleCommands = new LinkedList<>();

    // Runtime
    private static @Getter Thread commandThread;

    public static void init() {
        consoleCommands.add(new HelpConsoleCommand());
        consoleCommands.add(new StopConsoleCommand());

        startCommandThread();
    }

    public static void registerCommands(AbstractConsoleCommand... consoleCommand) {
        consoleCommands.addAll(List.of(consoleCommand));
    }

    private static void processCommand(String command) {
        if (command == null) {
            return;
        }

        ArgumentParser argumentParser = new ArgumentParser(command);

        if (!argumentParser.hasAnyArguments()) {
            Logger.error("Unknown command '" + command + "'!");
            return;
        }

        String name = argumentParser.getArgumentAtIndex(0).getValue();
        String arguments = "";

        if (argumentParser.hasArgumentAtIndex(1)) {
            arguments = argumentParser.getAllArgumentsAfterIndex(1).getValue();
        }

        for (AbstractConsoleCommand abstractConsoleCommand : consoleCommands) {
            if (abstractConsoleCommand.name.equalsIgnoreCase(name)) {
                try {
                    CommandResult commandResult = abstractConsoleCommand.execute(arguments);

                    if (commandResult == CommandResult.INCORRECT_SYNTAX) {
                        Logger.error("Invalid syntax! Syntax: " + abstractConsoleCommand.name + " " + abstractConsoleCommand.syntax);
                    }
                } catch (Exception exception) {
                    Logger.throwing(exception);
                    Logger.error("Exception occurred while executing command '" + command + "'!");
                }
                return;
            }
        }

        Logger.error("Unknown command '" + command + "'!");
    }

    private static void startCommandThread() {
        commandThread = new Thread(() -> {
            while (true) {
                String command = System.console().readLine();
                processCommand(command);
            }
        });
        commandThread.start();
    }
}
