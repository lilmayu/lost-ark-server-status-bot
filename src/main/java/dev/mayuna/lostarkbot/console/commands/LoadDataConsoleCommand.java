package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.utils.NumberUtils;

public class LoadDataConsoleCommand extends AbstractConsoleCommand {

    public LoadDataConsoleCommand() {
        this.name = "load-data";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length == 0) {
            Logger.error("Syntax: load-data <dashboards|config>");
            return;
        }

        switch (args[0]) {
            case "guilds" -> {
                GuildDataManager.loadAll();
                GuildDataManager.loadDashboards();
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

