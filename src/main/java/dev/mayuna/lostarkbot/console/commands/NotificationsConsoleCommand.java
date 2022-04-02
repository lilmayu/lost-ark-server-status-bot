package dev.mayuna.lostarkbot.console.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mayuna.lostarkbot.api.unofficial.Forums;
import dev.mayuna.lostarkbot.api.unofficial.News;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.console.commands.generic.CommandResult;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.objects.Notifications;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.ObjectUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.LostArk;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationsConsoleCommand extends AbstractConsoleCommand {

    public NotificationsConsoleCommand() {
        this.name = "notif";
        this.syntax = "<update|show-cache|show-unread|show-read|read-all|force-send-notifications|force-send-server-status-change|fake-server-status <server> <status>|fake-all-status <status>|clear-all-servers>";
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

                case "show-cache" -> {
                    News newsGeneral = NotificationsManager.getNewsGeneral();
                    News newsEvents = NotificationsManager.getNewsEvents();
                    News newsReleaseNotes = NotificationsManager.getNewsReleaseNotes();
                    News newsUpdates = NotificationsManager.getNewsUpdates();
                    Forums forumsMaintenance = NotificationsManager.getForumsMaintenance();
                    Forums forumsDowntime = NotificationsManager.getForumsDowntime();

                    if (ObjectUtils.allNotNull(newsGeneral, newsEvents, newsReleaseNotes, newsUpdates, forumsMaintenance, forumsDowntime)) {
                        Logger.success("All API objects are not null.");
                    }

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    
                    if (newsGeneral != null) {
                        Logger.info("NewsGeneral:");
                        Logger.info(gson.toJson(newsGeneral));
                    } else {
                        Logger.warn("News General is null.");
                    }

                    if (newsEvents != null) {
                        Logger.info("NewsEvents:");
                        Logger.info(gson.toJson(newsEvents));
                    } else {
                        Logger.warn("News Events is null.");
                    }

                    if (newsReleaseNotes != null) {
                        Logger.info("NewsReleaseNotes:");
                        Logger.info(gson.toJson(newsReleaseNotes));
                    } else {
                        Logger.warn("News Release Notes is null.");
                    }

                    if (newsUpdates != null) {
                        Logger.info("NewsUpdates:");
                        Logger.info(gson.toJson(newsUpdates));
                    } else {
                        Logger.warn("News Updates is null.");
                    }

                    if (forumsMaintenance != null) {
                        Logger.info("ForumsMaintenance:");
                        Logger.info(gson.toJson(forumsMaintenance));
                    } else {
                        Logger.warn("Forums Maintenance is null.");
                    }

                    if (forumsDowntime != null) {
                        Logger.info("ForumsDowntime:");
                        Logger.info(gson.toJson(forumsDowntime));
                    } else {
                        Logger.warn("Forums Downtime is null.");
                    }
                }

                case "show-unread" -> {
                    Notifications notifications = NotificationsManager.getUnreadNotifications();

                    int index = 0;

                    Logger.info("Unread News: " + notifications.getNews().size());
                    for (Map.Entry<NewsObject, NewsCategory> entry : notifications.getNews().entrySet()) {
                        Logger.info("[" + index++ + "]: " + entry.getValue().name() + " | " + entry.getKey().getTitle() + " (" + entry.getKey().hash() + ")");
                    }

                    index = 0;

                    Logger.info("Unread Forums: " + notifications.getForums().size());
                    for (Map.Entry<ForumsPostObject, ForumsCategory> entry : notifications.getForums().entrySet()) {
                        Logger.info("[" + index++ + "]: " + entry.getValue().name() + " | " + entry.getKey().getTitle() + " (" + entry.getKey().hash() + ")");
                    }
                }

                case "show-read" -> {
                    List<String> hashesList = NotificationsManager.HashCache.getLoadedHashesList();

                    if (hashesList == null) {
                        Logger.warn("There was error while reading hashes. (hashesList == null)");
                        return CommandResult.SUCCESS;
                    }

                    int index = 0;

                    Logger.info("Read notifications: " + hashesList.size());
                    for (String hash : hashesList) {
                        Logger.info("[" + index++ + "]: " + hash);
                    }
                }

                case "read-all" -> {
                    Logger.info("Marking all as read...");
                    Notifications notifications = NotificationsManager.getUnreadNotifications();

                    NotificationsManager.HashCache.setAsSent(notifications.getForums().keySet().toArray(new Hashable[0]));
                    NotificationsManager.HashCache.setAsSent(notifications.getNews().keySet().toArray(new Hashable[0]));

                    Logger.success("Done marking.");
                }

                case "force-send-notifications" -> {
                    Logger.info("Force sending notifications to channels...");

                    NotificationsManager.sendToAllNotificationChannelsByRules(NotificationsManager.getUnreadNotifications());

                    Logger.success("Sending done.");
                }

                case "force-send-server-status-change" -> {
                    Logger.info("Forcing Server Status Change...");

                    GuildDataManager.processServerStatusChange(ServerDashboardHelper.getPreviousServerCache(), ServerDashboardHelper.getLostArkServersCache());

                    Logger.info("Done.");
                }

                case "fake-server-status" -> {
                    String serverName = Utils.doesServerExist(argumentParser.getArgumentAtIndex(1).getValue());

                    if (serverName == null) {
                        Logger.error("This server does not exist!");
                        return CommandResult.SUCCESS;
                    }

                    ServerStatus serverStatus;

                    try {
                        serverStatus = ServerStatus.valueOf(argumentParser.getArgumentAtIndex(2).getValue());
                    } catch (Exception ignored) {
                        Logger.error("Invalid status!");
                        return CommandResult.SUCCESS;
                    }

                    LostArkServers lostArkServers = ServerDashboardHelper.getLostArkServersCache();
                    LostArkServer lostArkServer = Utils.getServerFromList(lostArkServers.getServers(), serverName);

                    lostArkServers.getServers().remove(lostArkServer);

                    LostArkServer lostArkServerNew = new LostArkServer(serverName, null, serverStatus);

                    lostArkServers.getServers().add(lostArkServerNew);

                    Logger.success("Successfully faked server status for " + serverName + " to " + serverStatus.name() + "!");
                }

                case "fake-all-status" -> {
                    ServerStatus serverStatus;

                    try {
                        serverStatus = ServerStatus.valueOf(argumentParser.getArgumentAtIndex(1).getValue());
                    } catch (Exception ignored) {
                        Logger.error("Invalid status!");
                        return CommandResult.SUCCESS;
                    }

                    LostArkServers lostArkServers = ServerDashboardHelper.getLostArkServersCache();
                    List<LostArkServer> servers = new LinkedList<>();

                    for (LostArkServer lostArkServer : lostArkServers.getServers()) {
                        servers.add(new LostArkServer(lostArkServer.getName(), null, serverStatus));
                    }

                    lostArkServers.getServers().clear();
                    lostArkServers.getServers().addAll(servers);

                    Logger.success("Successfully faked all server statuses to " + serverStatus.name() + "!");
                }

                case "clear-all-servers" -> {
                    ServerDashboardHelper.getLostArkServersCache().getServers().clear();

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
