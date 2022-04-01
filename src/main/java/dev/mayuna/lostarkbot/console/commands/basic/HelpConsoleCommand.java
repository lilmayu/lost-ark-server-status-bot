package dev.mayuna.lostarkbot.console.commands.basic;

import dev.mayuna.lostarkbot.console.ConsoleCommandManager;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.mayuslibrary.logging.Logger;

public class HelpConsoleCommand extends AbstractConsoleCommand {

    public HelpConsoleCommand() {
        this.name = "help";
        this.syntax = "";
    }

    @Override
    public CommandResult execute(String arguments) {
        Logger.info("=== Loaded Commands (" + ConsoleCommandManager.getConsoleCommands().size() + ") ===");
        for (var consoleCommand : ConsoleCommandManager.getConsoleCommands()) {
            Logger.info("> " + consoleCommand.name + " " + consoleCommand.syntax);
        }

        return CommandResult.SUCCESS;
    }
}
