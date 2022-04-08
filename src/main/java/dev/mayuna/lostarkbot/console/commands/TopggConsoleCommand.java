package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class TopggConsoleCommand extends AbstractConsoleCommand {

    public TopggConsoleCommand() {
        this.name = "topgg";
        this.syntax = "";
    }

    @Override
    public CommandResult execute(String arguments) {

        Logger.info("Testing TOP.GG API...");

        // Not implemented :(

        return CommandResult.SUCCESS;
    }
}
