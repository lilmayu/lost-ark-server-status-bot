package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class DebugConsoleCommand extends AbstractConsoleCommand {

    public DebugConsoleCommand() {
        this.name = "debug";
    }

    @Override
    public void execute(String arguments) {
        if (arguments.contains("force-update")) {
            Logger.info("Force updating all guilds...");

            GuildDataManager.updateAllGuildData();

            Logger.info("Done.");
            return;
        }

        Logger.info("=== Debug ===");
        Logger.info("JDA Guilds: " + Main.getJda().getGuilds().size());
        Logger.info("GuildData: " + GuildDataManager.getLoadedGuildDataList().size());
        Logger.info("Dashboards: " + GuildDataManager.countAllDashboards());
        Logger.info("In-game players: " + ServerDashboardHelper.getOnlinePlayersCache());
        Logger.info("");
        Logger.info("Run lost-ark command for lost-ark's debug");
    }
}
