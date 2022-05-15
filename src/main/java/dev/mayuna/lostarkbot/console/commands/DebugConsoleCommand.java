package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.managers.ShardExecutorManager;
import dev.mayuna.lostarkbot.util.HashUtils;
import dev.mayuna.lostarkbot.util.SpecialRateLimiter;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;

import java.util.Iterator;

public class DebugConsoleCommand extends AbstractConsoleCommand {

    public DebugConsoleCommand() {
        this.name = "debug";
        this.syntax = "[force-update|hash <to_hash>]";
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

                    String toHash = argumentParser.getAllArgumentsAfterIndex(1).toString();

                    Logger.info("Hash of '" + toHash + "' is '" + HashUtils.hashMD5(toHash));
                }

                case "data" -> {
                    Logger.info("=== Debug - Data ===");
                    Logger.info("GuildData: " + GuildDataManager.countGuildDataSize());
                    Logger.info("Dashboards: " + GuildDataManager.countAllDashboards());
                    Logger.info("Notifications ch.: " + GuildDataManager.countAllNotificationChannels());
                }
            }

            return CommandResult.SUCCESS;
        }

        Logger.info("=== Debug ===");

        Logger.info("== Discord ==");
        Logger.info("Shards: " + Main.getMayuShardManager().get().getShardsTotal());
        Logger.info("JDA Guilds: " + Main.getMayuShardManager().get().getGuilds().size());

        Logger.info("== Other ==");
        Logger.info("In-game players: " + ServerDashboardManager.getOnlinePlayersCache());
        Logger.info("SpecialRateLimiter: " + SpecialRateLimiter.getCurrentRequestCount() + " requests (last " + SpecialRateLimiter.getLastRequestCount() + " requests)");

        Logger.info("== Executor ==");
        Logger.info("Currently running tasks: " + ShardExecutorManager.getExecutorService().getActiveCount());
        Logger.info("Queued tasks: " + ShardExecutorManager.getExecutorService().getQueue().size());
        Logger.info("Total task count: " + ShardExecutorManager.getExecutorService().getTaskCount());

        if (!ShardExecutorManager.getRunningTasks().isEmpty()) {
            Logger.info("= Running tasks - " + ShardExecutorManager.getRunningTasks().size() + " =");
            Iterator<ShardExecutorManager.Task> iterator = ShardExecutorManager.getRunningTasks().listIterator();
            while (iterator.hasNext()) {
                ShardExecutorManager.Task task = iterator.next();

                Logger.info("> " + task.shardId() + " | " + task.updateType() + " | Running for " + Utils.getTimerWithoutMillis(task.getElapsedTime()));
            }
        }

        Logger.info("");
        Logger.info("Type debug data for data-related debug");

        return CommandResult.SUCCESS;
    }
}
