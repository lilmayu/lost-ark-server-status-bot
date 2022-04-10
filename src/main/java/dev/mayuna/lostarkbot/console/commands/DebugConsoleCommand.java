package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.managers.ShardExecutorManager;
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

                    GuildDataManager.updateAllServerDashboards();

                    Logger.success("Updating done.");
                }

                case "hash" -> {
                    if (!argumentParser.hasArgumentAtIndex(1)) {
                        return CommandResult.INCORRECT_SYNTAX;
                    }

                    String toHash = argumentParser.getAllArgumentsAfterIndex(0).toString();

                    Logger.info("Hash of '" + toHash + "' is '" + HashUtils.hashMD5(toHash));
                }
            }

            return CommandResult.SUCCESS;
        }

        Logger.info("=== Debug ===");
        Logger.info("Shards: " + Main.getMayuShardManager().get().getShardsTotal());
        Logger.info("JDA Guilds: " + Main.getMayuShardManager().get().getGuilds().size());
        Logger.info("GuildData: " + GuildDataManager.countGuildDataSize());
        Logger.info("Dashboards: " + GuildDataManager.countAllDashboards());
        Logger.info("Notifications ch.: " + GuildDataManager.countAllNotificationChannels());
        Logger.info("In-game players: " + ServerDashboardManager.getOnlinePlayersCache());
        Logger.info("Currently running tasks: " + ShardExecutorManager.getCurrentlyRunningTasks());

        return CommandResult.SUCCESS;
    }
}
