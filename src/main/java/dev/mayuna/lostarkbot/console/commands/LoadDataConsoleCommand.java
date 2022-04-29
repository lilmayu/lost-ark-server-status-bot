package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;

public class LoadDataConsoleCommand extends AbstractConsoleCommand {

    public LoadDataConsoleCommand() {
        this.name = "load-data";
        this.syntax = "<guilds|config|hashes|lang>";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "guilds" -> {
                    GuildDataManager.loadAllFiles();
                    GuildDataManager.loadAllGuildDataFeatures();

                    Logger.success("Loading done.");
                }
                case "config" -> {
                    Logger.info("Loading config...");
                    Config.load();
                    Logger.success("Loading done.");
                }
                case "hashes" -> {
                    Logger.info("Loading hashes...");
                    NotificationsManager.HashCache.loadHashes();
                    Logger.success("Loading done.");
                }
                case "lang" -> {
                    Logger.info("Loading languages...");
                    LanguageManager.load();
                    Logger.success("Loading done.");
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

