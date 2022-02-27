package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.DataManager;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class SaveDataConsoleCommand extends AbstractConsoleCommand {

    public SaveDataConsoleCommand() {
        this.name = "save-data";
    }

    @Override
    public void execute(String arguments) {
        switch (arguments) {
            case "guilds" -> {
                DataManager.saveAll();
            }
            case "guild" -> {
                // TODO: Savenutí jedné specifické guildy (podle ID)
            }
            case "config" -> {
                Config.save();
            }
            default -> {
                Logger.error("Syntax: save-data <dashboards|config>");
            }
        }
    }
}

