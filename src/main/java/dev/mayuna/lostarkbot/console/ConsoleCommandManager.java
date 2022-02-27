package dev.mayuna.lostarkbot.console;

import dev.mayuna.lostarkbot.console.commands.*;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import dev.mayuna.mayuslibrary.logging.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ConsoleCommandManager {

    // Data
    private static @Getter final List<AbstractConsoleCommand> consoleCommands = new ArrayList<>();

    // Runtime
    private static @Getter Thread commandThread;

    public static void init() {
        consoleCommands.add(new HelpConsoleCommand());
        consoleCommands.add(new StopConsoleCommand());
        consoleCommands.add(new GuildsConsoleCommand());
        consoleCommands.add(new LostArkConsoleCommand());
        consoleCommands.add(new SaveDataConsoleCommand());
        consoleCommands.add(new LoadDataConsoleCommand());
        consoleCommands.add(new DebugConsoleCommand());
        consoleCommands.add(new LangConsoleCommand());

        startCommandThread();
    }

    public static void registerCommand(AbstractConsoleCommand consoleCommand) {
        consoleCommands.add(consoleCommand);
    }

    private static void processCommand(String command) {
        if (command == null) {
            return;
        }

        ArgumentParser argumentParser = new ArgumentParser(command);

        if (!argumentParser.hasAnyArguments()) {
            Logger.error("Unknown command.");
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
                    abstractConsoleCommand.execute(arguments);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Exception occurred while executing command '" + command + "'!");
                }
                return;
            }
        }

        Logger.error("Unknown command.");
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