package dev.mayuna.lostarkbot.console.commands.basic;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class StopConsoleCommand extends AbstractConsoleCommand {

    public StopConsoleCommand() {
        this.name = "stop";
        this.syntax = "";
    }

    @Override
    public CommandResult execute(String arguments) {
        Main.setStopping(true);
        Logger.info("Saving all Guilds before stopping...");
        GuildDataManager.saveAll();
        System.exit(0);
        return CommandResult.SUCCESS;
    }
}
