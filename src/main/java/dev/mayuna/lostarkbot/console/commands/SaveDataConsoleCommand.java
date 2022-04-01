package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import dev.mayuna.mayuslibrary.utils.NumberUtils;

public class SaveDataConsoleCommand extends AbstractConsoleCommand {

    public SaveDataConsoleCommand() {
        this.name = "save-data";
        this.syntax = "<guilds|config>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "guilds" -> {
                    GuildDataManager.saveAll();
                    Logger.success("Saving done.");
                }
                case "config" -> {
                    Logger.info("Saving config...");
                    Config.save();
                    Logger.success("Saving done.");
                }
                default -> {
                    return CommandResult.INCORRECT_SYNTAX;
                }
            }

            return CommandResult.SUCCESS;
        }

        return CommandResult.INCORRECT_SYNTAX;
    }
}

