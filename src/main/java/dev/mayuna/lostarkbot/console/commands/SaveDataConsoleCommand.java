package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;

public class SaveDataConsoleCommand extends AbstractConsoleCommand {

    public SaveDataConsoleCommand() {
        this.name = "save-data";
    }

    @Override
    public void execute(String arguments) {
        ServerDashboardManager.save();
    }
}

