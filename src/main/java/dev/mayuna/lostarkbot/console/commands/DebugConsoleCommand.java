package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class DebugConsoleCommand extends AbstractConsoleCommand {

    public DebugConsoleCommand() {
        this.name = "debug";
    }

    @Override
    public void execute(String arguments) {
        if (arguments.contains("force-update")) {
            Logger.info("Force updating all dashboards...");

            ServerDashboardManager.updateAll();
            return;
        }

        Logger.info("=== Debug ===");
        Logger.info("Guilds: " + Main.getJda().getGuilds().size());
        Logger.info("Dashboards: " + ServerDashboardManager.getDashboards().size());
        Logger.info("In-game players: " + ServerDashboardManager.getOnlinePlayersCache());
        Logger.info("");
        Logger.info("Run lost-ark command for lost-ark's debug");
    }
}
