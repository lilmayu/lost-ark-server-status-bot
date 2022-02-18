package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;

public class LoadDataConsoleCommand extends AbstractConsoleCommand {

    public LoadDataConsoleCommand() {
        this.name = "load-data";
    }

    @Override
    public void execute(String arguments) {
        ServerDashboardManager.load();
    }
}

