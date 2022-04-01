package dev.mayuna.lostarkbot.console.commands.basic;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;

public class StopConsoleCommand extends AbstractConsoleCommand {

    public StopConsoleCommand() {
        this.name = "stop";
        this.syntax = "";
    }

    @Override
    public CommandResult execute(String arguments) {
        System.exit(0);
        return CommandResult.SUCCESS;
    }
}
