package dev.mayuna.lostarkbot.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.old.api.ApiRestAction;
import dev.mayuna.lostarkbot.old.api.misc.ApiResponse;
import dev.mayuna.lostarkbot.old.api.unofficial.Forums;
import dev.mayuna.lostarkbot.old.api.unofficial.News;
import dev.mayuna.lostarkbot.old.api.unofficial.UnofficialLostArkApi;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.objects.other.Notifications;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class NotificationsManager {

    private static final @Getter Timer notificationsUpdateWorker = new Timer("NotificationsUpdateWorker");

    private static @Getter News newsGeneral;
    private static @Getter News newsEvents;
    private static @Getter News newsReleaseNotes;
    private static @Getter News newsUpdates;

    private static @Getter Forums forumsMaintenance;
    private static @Getter Forums forumsDowntime;

    public static void init() {
        Logger.info("[NOTIFICATIONS] Initializing Notification manager...");
        HashCache.loadHashes();

        notificationsUpdateWorker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Logger.info("Updating Notifications..");
                    fetchAll();

                    Logger.flow("Checking if there are new notifications...");
                    processPotentialNewNotifications();
                } catch (Exception exception) {
                    Logger.throwing(exception);
                    ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), exception);

                    Logger.error("Exception occurred while updating notifications!");
                }
            }
        }, 2000, 300000);
    }

    public static void processPotentialNewNotifications() {
        Notifications notifications = getUnreadNotifications();

        if (!notifications.isThereAnyUnreadNotification()) {
            Logger.debug("There are no new notifications.");
            return;
        }

        Logger.info("There are new notifications! Sending them to Notification channels...");
        Logger.debug("News: " + notifications.getNews().size());
        Logger.debug("Forums: " + notifications.getForums().size());

        long start = System.currentTimeMillis();
        sendToAllNotificationChannelsByRules(notifications);
        long took = System.currentTimeMillis() - start;

        HashCache.setAsSent(notifications.getForums().keySet().toArray(new Hashable[0]));
        HashCache.setAsSent(notifications.getNews().keySet().toArray(new Hashable[0]));

        Logger.info("Queuing notifications messages for all shards done in " + took + "ms.");
    }

    public static void sendToAllNotificationChannelsByRules(Notifications notifications) {
        ShardExecutorManager.submitForEachShard(UpdateType.NOTIFICATIONS, shardId -> {
            GuildDataManager.processAllGuildDataWithNotifications(shardId, notifications);
        });
    }

    public static void fetchAll() {
        UnofficialLostArkApi api = new UnofficialLostArkApi();

        newsGeneral = fetch(api.fetchNews(NewsCategory.GENERAL), "News GENERAL");
        newsEvents = fetch(api.fetchNews(NewsCategory.EVENTS), "News EVENTS");
        newsReleaseNotes = fetch(api.fetchNews(NewsCategory.RELEASE_NOTES), "News RELEASE_NOTES");
        newsUpdates = fetch(api.fetchNews(NewsCategory.UPDATES), "News UPDATES");

        forumsMaintenance = fetch(api.fetchForumPosts(ForumsCategory.MAINTENANCE), "Forums Maintenance");
        forumsDowntime = fetch(api.fetchForumPosts(ForumsCategory.DOWNTIME), "Forums Downtime");

        if (ObjectUtils.allNotNull(newsGeneral, newsEvents, newsReleaseNotes, newsUpdates, forumsMaintenance, forumsDowntime)) {
            Logger.success("Successfully fetched all API objects.");
        } else {
            Logger.error("Some API objects are null! true == null");
            Logger.debug("newsGeneral: " + (newsGeneral == null));
            Logger.debug("newsEvents: " + (newsEvents == null));
            Logger.debug("newsReleaseNotes: " + (newsReleaseNotes == null));
            Logger.debug("newsUpdates: " + (newsUpdates == null));
            Logger.debug("forumsMaintenance: " + (forumsMaintenance == null));
            Logger.debug("forumsDowntime: " + (forumsDowntime == null));
        }

        if (forumsMaintenance != null) {
            List<ForumsPostObject> toRemoveFromMaintenance = new ArrayList<>();
            for (ForumsPostObject forumsPostObject : forumsMaintenance.getForumsPostObjects()) {
                if (forumsPostObject.getTitle().startsWith("[Downtime]")) {
                    toRemoveFromMaintenance.add(forumsPostObject);
                }
            }
            forumsMaintenance.remove(toRemoveFromMaintenance);
        }
    }

    public static Notifications getUnreadNotifications() {
        Notifications notifications = new Notifications();

        notifications.add(getUnreadNotifications(newsGeneral, NewsCategory.GENERAL));
        notifications.add(getUnreadNotifications(newsEvents, NewsCategory.EVENTS));
        notifications.add(getUnreadNotifications(newsReleaseNotes, NewsCategory.RELEASE_NOTES));
        notifications.add(getUnreadNotifications(newsUpdates, NewsCategory.UPDATES));

        notifications.add(getUnreadNotifications(forumsMaintenance, ForumsCategory.MAINTENANCE));
        notifications.add(getUnreadNotifications(forumsDowntime, ForumsCategory.DOWNTIME));

        return notifications;
    }

    public static Notifications getUnreadNotifications(News news, NewsCategory category) {
        Notifications notifications = new Notifications();

        if (news == null || news.getNewsObjects() == null) {
            return notifications;
        }

        for (NewsObject newsObject : news.getNewsObjects()) {
            if (HashCache.wasSent(newsObject) == Result.FALSE) {
                notifications.addNews(newsObject, category);
            }
        }

        return notifications;
    }

    public static Notifications getUnreadNotifications(Forums forums, ForumsCategory category) {
        Notifications notifications = new Notifications();

        if (forums == null || forums.getForumsPostObjects() == null) {
            return notifications;
        }

        for (ForumsPostObject forumsPostObject : forums.getForumsPostObjects()) {
            if (HashCache.wasSent(forumsPostObject) == Result.FALSE) {
                notifications.addForums(forumsPostObject, category);
            }
        }

        return notifications;
    }

    private static <T extends ApiResponse> T fetch(ApiRestAction<T> restAction, String infoType) {
        Logger.flow("[REQUESTER] Requesting " + infoType + " from API...");

        AtomicReference<T> atomicReference = new AtomicReference<>();

        AtomicInteger waitTime = new AtomicInteger(0);
        AtomicInteger retries = new AtomicInteger(0);
        AtomicBoolean canContinue = new AtomicBoolean(false);

        do {
            if (retries.get() >= 10) {
                Logger.error("[REQUESTER] Failed to fetch " + infoType + " after 10 retries!");
                return null;
            }

            restAction.onHttpError(httpError -> {
                Logger.throwing(httpError.getException());
                Logger.warn("[REQUESTER] HTTP Error occurred while requesting " + infoType + "! (retry " + retries.get() + ") Code: " + httpError.getCode());

                waitTime.set(10000);
                retries.addAndGet(1);
            });
            restAction.onApiError(apiError -> {
                Logger.warn("[REQUESTER] API Error occurred while requesting " + infoType + "! (retry " + retries.get() + ") Code: " + apiError.getError());

                waitTime.set(10000);
                retries.addAndGet(1);
            });
            restAction.onSuccess((jsonObject, response) -> {
                Logger.info("[REQUESTER] Request for " + infoType + " was successful.");
                atomicReference.set(response);

                waitTime.set(0);
                canContinue.set(true);
            });

            restAction.execute();

            if (waitTime.get() > 0) {
                try {
                    Thread.sleep(waitTime.get());
                } catch (Exception exception) {
                    Logger.throwing(exception);

                    Logger.fatal("Thread was interrupted while requesting/sleeping.");
                }
            }
        } while (!canContinue.get());

        return atomicReference.get();
    }

    public static class HashCache {

        private static MayuJson loadedHashesCache;

        public static MayuJson getLoadedHashesCache() {
            if (loadedHashesCache == null) {
                loadHashes();
            }

            return loadedHashesCache;
        }

        public static List<String> getLoadedHashesList() {
            MayuJson mayuJson = getLoadedHashesCache();

            if (mayuJson == null) {
                return null; // Jelikož nechcem posílat něco, co potencionálně se poslalo, jen kvůli tomu, že je tam chyba...
            }

            JsonArray hashesArray = mayuJson.getOrCreate("hashes", new JsonArray()).getAsJsonArray();
            List<String> hashesList = new LinkedList<>();

            for (JsonElement jsonElement : hashesArray) {
                hashesList.add(jsonElement.getAsString());
            }

            return hashesList;
        }

        public static boolean loadHashes() {
            MayuJson mayuJson = getPostsHashesMayuJson();

            if (mayuJson == null) {
                return false;
            }

            loadedHashesCache = mayuJson;

            return true;
        }

        public static Result wasSent(Hashable hashable) {
            List<String> loadedHashes = getLoadedHashesList();

            if (loadedHashes == null) {
                return Result.ERROR;
            }

            String hash = hashable.hash();

            for (String jsonHash : loadedHashes) {
                if (HashUtils.equalsHashHash(hash, jsonHash)) {
                    return Result.TRUE;
                }
            }

            return Result.FALSE;
        }

        public static Result setAsSent(Hashable... hashables) {
            if (hashables == null || hashables.length == 0) {
                return Result.FALSE;
            }

            MayuJson mayuJson = getLoadedHashesCache();

            if (mayuJson == null) {
                return Result.ERROR;
            }

            JsonArray hashesArray = mayuJson.getOrCreate("hashes", new JsonArray()).getAsJsonArray();

            for (Hashable hashable : hashables) {
                String hash = hashable.hash();
                if (!hashesArray.contains(new JsonPrimitive(hash))) {
                    hashesArray.add(hash);
                }
            }

            mayuJson.add("hashes", hashesArray);

            if (JsonUtils.saveMayuJson(mayuJson)) {
                return Result.TRUE;
            }

            return Result.ERROR;
        }

        private static MayuJson getPostsHashesMayuJson() {
            try {
                return JsonUtil.createOrLoadJsonFromFile(Constants.POSTS_HASHES_JSON);
            } catch (Exception exception) {
                Logger.throwing(exception);

                Logger.fatal("Could not read " + Constants.POSTS_HASHES_JSON + " file!");
                return null;
            }
        }
    }
}
