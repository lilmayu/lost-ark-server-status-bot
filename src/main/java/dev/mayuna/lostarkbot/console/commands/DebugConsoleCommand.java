package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.util.HashUtils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;

public class DebugConsoleCommand extends AbstractConsoleCommand {

    public DebugConsoleCommand() {
        this.name = "debug";
        this.syntax = "[force-update]";
    }

    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "force-update" -> {
                    Logger.info("Force updating all guilds...");

                    GuildDataManager.updateAllGuildData();

                    Logger.success("Updating done.");
                }
            }

            return CommandResult.SUCCESS;
        }

        Logger.info("=== Debug ===");
        Logger.info("JDA Guilds: " + Main.getJda().getGuilds().size());
        Logger.info("GuildData: " + GuildDataManager.getLoadedGuildDataList().size());
        Logger.info("Dashboards: " + GuildDataManager.countAllDashboards());
        Logger.info("Notifications ch.: " + GuildDataManager.countAllNotificationChannels());
        Logger.info("In-game players: " + ServerDashboardHelper.getOnlinePlayersCache());

        return CommandResult.SUCCESS;
    }
}
