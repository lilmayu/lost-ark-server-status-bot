package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.objects.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.Notifications;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.LostArk;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import dev.mayuna.mayuslibrary.arguments.ArgumentSeparator;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationsConsoleCommand extends AbstractConsoleCommand {

    public NotificationsConsoleCommand() {
        this.name = "notif";
    }

    @SneakyThrows
    @Override
    public void execute(String arguments) {
        ArgumentParser argumentParser = new ArgumentParser(arguments, ArgumentSeparator.SPACE);

        if (!argumentParser.hasAnyArguments() || !argumentParser.hasArgumentAtIndex(0)) {
            Logger.error("Invalid syntax! Syntax: notif <update|show-unread|show-read|read-all|force-send|reload-cache|fake-server-status|force-server-status-change|fake-all-status|all-gone>");
            return;
        }

        String action = argumentParser.getArgumentAtIndex(0).getValue();

        switch (action) {
            case "update" -> {
                NotificationsManager.fetchAll();

                Logger.info("Done fetching.");
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
                    Logger.warn("There was error while reading hashes.");
                    return;
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

                Logger.info("Marked.");
            }

            case "force-send" -> {
                Logger.info("Forcing...");

                NotificationsManager.sendToAllNotificationChannelsByRules(NotificationsManager.getUnreadNotifications());

                Logger.info("Done.");
            }

            case "reload-cache" -> {
                Logger.info("Reloading cache...");

                NotificationsManager.HashCache.loadHashes();

                Logger.info("Done.");
            }

            case "fake-server-status" -> {
                if (!argumentParser.hasArgumentAtIndex(2)) {
                    Logger.error("Invalid syntax! Syntax: notif <fake-server-status> <server_name> <status>");
                    return;
                }

                String serverName = Utils.doesServerExist(argumentParser.getArgumentAtIndex(1).getValue());

                if (serverName == null) {
                    Logger.error("This server does not exist!");
                    return;
                }

                ServerStatus serverStatus;

                try {
                    serverStatus = ServerStatus.valueOf(argumentParser.getArgumentAtIndex(2).getValue());
                } catch (Exception ignored) {
                    Logger.error("Invalid status!");
                    return;
                }

                LostArkServers lostArkServers = ServerDashboardHelper.getLostArkServersCache();
                LostArkServer lostArkServer = Utils.getServerFromList(lostArkServers.getServers(), serverName);

                lostArkServers.getServers().remove(lostArkServer);

                LostArkServer lostArkServerNew = new LostArkServer(serverName, null, serverStatus);

                lostArkServers.getServers().add(lostArkServerNew);

                Logger.success("Successfully faked server status for " + serverName + " to " + serverStatus.name() + "!");
            }

            case "fake-all-status" -> {
                if (!argumentParser.hasArgumentAtIndex(1)) {
                    Logger.error("Invalid syntax! Syntax: notif <fake-server-status> <status>");
                    return;
                }

                ServerStatus serverStatus;

                try {
                    serverStatus = ServerStatus.valueOf(argumentParser.getArgumentAtIndex(1).getValue());
                } catch (Exception ignored) {
                    Logger.error("Invalid status!");
                    return;
                }

                LostArkServers lostArkServers = ServerDashboardHelper.getLostArkServersCache();
                List<LostArkServer> servers = new LinkedList<>();

                for (LostArkServer lostArkServer : lostArkServers.getServers()) {
                    servers.add(new LostArkServer(lostArkServer.getName(), null, serverStatus));
                }

                lostArkServers.getServers().clear();
                lostArkServers.getServers().addAll(servers);

                Logger.success("Successfully faked server all statuses to " + serverStatus.name() + "!"); // ugh i am lazy to write in proper english
            }

            case "all-gone" -> {
                ServerDashboardHelper.getLostArkServersCache().getServers().clear();

                Logger.success("Successfully removed all servers from cache!");
            }

            case "force-server-status-change" -> {
                Logger.info("Forcing Server Status Change...");

                GuildDataManager.processServerStatusChange(ServerDashboardHelper.getLostArkServersCache(), LostArk.fetchServers());

                Logger.info("Done.");
            }
        }
    }
}
