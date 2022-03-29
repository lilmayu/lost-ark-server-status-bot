package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.util.HashUtils;
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

        if (arguments.contains(" ")) {
            String[] args = arguments.split(" ");

            if (args.length < 2) {
                Logger.error("Invalid syntax! Syntax: <hash> <string>");
                return;
            }

            switch (args[0]) {
                case "hash" -> {
                    Logger.info("Hash of '" + args[1] + "' is '" + HashUtils.hashMD5(args[1]) +"'");
                }
            }


            return;
        }

        Logger.info("=== Debug ===");
        Logger.info("JDA Guilds: " + Main.getJda().getGuilds().size());
        Logger.info("GuildData: " + GuildDataManager.getLoadedGuildDataList().size());
        Logger.info("Dashboards: " + GuildDataManager.countAllDashboards());
        Logger.info("Notifications ch.: " + GuildDataManager.countAllNotificationChannels());
        Logger.info("In-game players: " + ServerDashboardHelper.getOnlinePlayersCache());
        Logger.info("");
        Logger.info("Run lost-ark command for lost-ark's debug");
    }
}
