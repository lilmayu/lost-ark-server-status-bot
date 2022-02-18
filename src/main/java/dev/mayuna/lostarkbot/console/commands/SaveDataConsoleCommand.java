package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class SaveDataConsoleCommand extends AbstractConsoleCommand {

    public SaveDataConsoleCommand() {
        this.name = "save-data";
    }

    @Override
    public void execute(String arguments) {
        switch (arguments) {
            case "dashboards" -> {
                ServerDashboardManager.save();
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

