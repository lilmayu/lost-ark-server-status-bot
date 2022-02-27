package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.DataManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class LoadDataConsoleCommand extends AbstractConsoleCommand {

    public LoadDataConsoleCommand() {
        this.name = "load-data";
    }

    @Override
    public void execute(String arguments) {
        switch (arguments) {
            case "guilds" -> {
                DataManager.load();
            }
            case "guild" -> {
                // TODO: Loadnutí jedné specifické guildy (podle ID)
            }
            case "config" -> {
                Logger.info("Loading config...");
                Config.load();
                Logger.success("Loading done.");
            }
            default -> {
                Logger.error("Syntax: load-data <dashboards|config>");
            }
        }
    }
}

