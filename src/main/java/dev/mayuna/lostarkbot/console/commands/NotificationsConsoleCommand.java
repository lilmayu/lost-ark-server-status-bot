package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.managers.ShardExecutorManager;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumCategoryName;
import dev.mayuna.lostarkbot.util.UpdateType;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkServerStatus;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationsConsoleCommand extends AbstractConsoleCommand {

    public NotificationsConsoleCommand() {
        this.name = "notif";
        this.syntax = "<update|show-cache-news|show-cache-forums|show-cache-forum-names|force-send-notifications|force-send-server-status-change|fake-all-status <status>|clear-all-servers>";
    }

    @SneakyThrows
    @Override
    public CommandResult execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments);

        if (argumentParser.hasArgumentAtIndex(0)) {
            switch (argumentParser.getArgumentAtIndex(0).getValue()) {
                case "update" -> {
                    Logger.info("Updating notifications cache...");
                    NotificationsManager.fetchAll();
                    Logger.info("Done updating.");
                }

                case "show-cache-news" -> {
                    Logger.info("> News ========");
                    for (LostArkNews[] lostArkNewsArray : NotificationsManager.getNews().values()) {
                        for (LostArkNews lostArkNews : lostArkNewsArray) {
                            Logger.info(lostArkNews.getTitle() + " (" + lostArkNews.getTag() + ") - " + lostArkNews.getPublishDate() + " (" + Hashable.create(
                                    lostArkNews).hash() + ")");
                        }
                    }

                    Logger.info("Listing done.");
                }
                case "show-cache-forum-names" -> {
                    Logger.info("> Forum names ========");
                    for (WrappedForumCategoryName wrappedForumCategoryName : NotificationsManager.getForumCategoryNames()) {
                        Logger.info("(" + wrappedForumCategoryName.getId() + ") - " + wrappedForumCategoryName.getName() + "(" + wrappedForumCategoryName.getVerboseName() + ")");

                        if (wrappedForumCategoryName.hasSubcategories()) {
                            Logger.info("^ Has subcategories.");

                            for (WrappedForumCategoryName wrappedForumCategoryName1 : wrappedForumCategoryName.getSubcategories()) {
                                Logger.info("^^ (" + wrappedForumCategoryName1.getId() + ") - " + wrappedForumCategoryName1.getName() + "(" + wrappedForumCategoryName1.getVerboseName() + ")");
                            }
                        }
                    }
                }
                case "show-cache-forums" -> {
                    Logger.info("> Forums ==========");

                    for (Map.Entry<Integer, LostArkForum> entry : NotificationsManager.getForums().entrySet()) {
                        Logger.info("[" + entry.getKey() + "]: Parsed name: " + entry.getValue().getTopicList().parseNameFromMoreTopicsUrl());
                        Logger.info("^ Topic list");

                        for (LostArkForum.TopicList.Topic topic : entry.getValue().getTopicList().getTopics()) {
                            Logger.info("- " + topic.getTitle() + " (" + topic.getId() + ") - " + topic.getCreatedAt());
                        }
                    }
                }

                case "force-send-notifications" -> {
                    Logger.info("Force sending notifications to channels...");

                    NotificationsManager.sendToAllNotificationChannelsByRules(NotificationsManager.getUnreadNotifications());

                    Logger.success("Sending done.");
                }


                case "force-send-server-status-change" -> {
                    Logger.info("Forcing Server Status Change...");

                    ShardExecutorManager.submitForEachShard(UpdateType.SERVER_STATUS, shardId -> {
                        GuildDataManager.processServerStatusChange(shardId, ServerDashboardManager.getPreviousLostArkServersCache(), ServerDashboardManager.getCurrentLostArkServersCache());
                    });

                    Logger.info("Done.");
                }

                case "fake-all-status" -> {
                    LostArkServerStatus serverStatus;

                    try {
                        serverStatus = LostArkServerStatus.valueOf(argumentParser.getArgumentAtIndex(1).getValue());
                    } catch (Exception ignored) {
                        Logger.error("Invalid status!");
                        return CommandResult.SUCCESS;
                    }

                    LostArkServers lostArkServers = ServerDashboardManager.getCurrentLostArkServersCache();
                    List<LostArkServer> servers = new LinkedList<>();

                    for (LostArkServer lostArkServer : lostArkServers.get()) {
                        servers.add(new LostArkServer(lostArkServer.getName(), lostArkServer.getRegion(), serverStatus));
                    }

                    lostArkServers.get().clear();
                    lostArkServers.get().addAll(servers);

                    Logger.success("Successfully faked all server statuses to " + serverStatus.name() + "!");
                }

                case "clear-all-servers" -> {
                    ServerDashboardManager.getCurrentLostArkServersCache().get().clear();

                    Logger.success("Successfully removed all servers from cache!");
                }

                default -> {
                    return CommandResult.INCORRECT_SYNTAX;
                }
            }

            return CommandResult.SUCCESS;
        }

        return CommandResult.INCORRECT_SYNTAX;
    }
}
