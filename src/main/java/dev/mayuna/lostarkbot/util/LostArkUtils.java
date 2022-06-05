package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.managers.NotificationsManager;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumCategoryName;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;

import java.util.ArrayList;
import java.util.List;

public class LostArkUtils {

    public static boolean doLostArkServersEqual(LostArkServers first, LostArkServers second) {
        if (first.get().size() != second.get().size()) {
            return false;
        }

        List<String> serverNames = new ArrayList<>();
        first.get().forEach(lostArkServer -> {
            serverNames.add(lostArkServer.getName());
        });

        second.get().forEach(lostArkServer -> {
            serverNames.remove(lostArkServer.getName());
        });

        return serverNames.size() == 0;
    }

    public static boolean doLostArkServersStatusesEqual(LostArkServers first, LostArkServers second) {
        if (!doLostArkServersEqual(first, second)) {
            return false;
        }

        for (LostArkServer firstServer : first.get()) {
            LostArkServer secondServer = second.getServerByName(firstServer.getName()).orElse(null);

            if (secondServer == null) {
                return false;
            }

            if (firstServer.getStatus() != secondServer.getStatus()) {
                return false;
            }
        }

        for (LostArkServer firstServer : second.get()) {
            LostArkServer secondServer = first.getServerByName(firstServer.getName()).orElse(null);

            if (secondServer == null) {
                return false;
            }

            if (firstServer.getStatus() != secondServer.getStatus()) {
                return false;
            }
        }

        return true;
    }

    public static LostArkServer getServerFromListByName(String serverName, List<LostArkServer> servers) {
        for (LostArkServer server : servers) {
            if (server.is(serverName)) {
                return server;
            }
        }

        return null;
    }

    public static String hashLostArkNews(LostArkNews lostArkNews) {
        return HashUtils.hashMD5(lostArkNews.getTitle() + "_" + lostArkNews.getPublishDate());
    }

    public static String getForumCategoryName(int forumCategoryId, LostArkForum lostArkForum) {
        String forumName = lostArkForum.getTopicList().parseNameFromMoreTopicsUrl();

        if (forumName == null) {
            forumName = "Forum ID " + forumCategoryId;
        }

        return forumName;
    }

    public static WrappedForumCategoryName getForumCategoryName(int forumCategoryId) {
        synchronized (NotificationsManager.getForumCategoryNames()) {
            for (WrappedForumCategoryName categoryName : NotificationsManager.getForumCategoryNames()) {
                if (categoryName.getId() == forumCategoryId) {
                    return categoryName;
                }

                if (categoryName.hasSubcategories()) {
                    for (WrappedForumCategoryName subCategoryName : categoryName.getSubcategories()) {
                        if (subCategoryName.getId() == forumCategoryId) {
                            return subCategoryName;
                        }
                    }
                }
            }
        }

        return new WrappedForumCategoryName(forumCategoryId, "Unknown Forum ID: " + forumCategoryId);
    }
}
