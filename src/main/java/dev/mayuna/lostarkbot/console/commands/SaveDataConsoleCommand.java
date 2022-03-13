package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.utils.NumberUtils;

public class SaveDataConsoleCommand extends AbstractConsoleCommand {

    public SaveDataConsoleCommand() {
        this.name = "save-data";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length == 0) {
            Logger.error("Syntax: save-data <guilds|config>");
            return;
        }

        switch (args[0]) {
            case "guilds" -> {
                GuildDataManager.saveAll();
            }
            case "config" -> {
                Config.save();
            }
            default -> {
                Logger.error("Syntax: save-data <guilds|config");
            }
        }
    }
}

