package dev.mayuna.lostarkbot.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.objects.abstracts.Hashable;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumCategoryName;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumTopic;
import dev.mayuna.lostarkbot.objects.other.Notifications;
import dev.mayuna.lostarkbot.objects.other.StaticNewsTags;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.LostArkFetcher;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkNewsTag;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class NotificationsManager {

    private static final @Getter Timer notificationsUpdateWorker = new Timer("NotificationsUpdateWorker");

    private static @Getter Map<StaticNewsTags, LostArkNews[]> news = new HashMap<>(); // News tag : Array of news
    private static @Getter Map<Integer, LostArkForum> forums = new HashMap<>(); // Forum Category Id : Forum

    private static @Getter List<WrappedForumCategoryName> forumCategoryNames = Collections.synchronizedList(new LinkedList<>());

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
        Logger.debug("Forums: " + notifications.getForumTopics().size());

        long start = System.currentTimeMillis();
        sendToAllNotificationChannelsByRules(notifications);
        long took = System.currentTimeMillis() - start;

        Logger.info("Queuing notifications messages for all shards done in " + took + "ms.");
    }

    public static void sendToAllNotificationChannelsByRules(Notifications notifications) {
        ShardExecutorManager.submitForEachShard(UpdateType.NOTIFICATIONS, shardId -> {
            GuildDataManager.processAllGuildDataWithNotifications(shardId, notifications);
        });
    }

    public static void fetchAll() {
        LostArkFetcher lostArkFetcher = Main.getLostArkFetcher();

        long start = System.currentTimeMillis();

        Logger.debug("[NOTIFICATIONS] Fetching news...");
        news.clear();
        forums.clear();

        synchronized (forumCategoryNames) {
            forumCategoryNames.clear();
        }

        lostArkFetcher.fetchNewsTags().execute().whenComplete((lostArkNewsTags, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                Logger.error("[NOTIFICATIONS] There was an exception while getting news tags!");
                return;
            }

            for (LostArkNewsTag newsTag : lostArkNewsTags) {
                StaticNewsTags staticNewsTags = StaticNewsTags.get(newsTag.getDisplayName());

                if (staticNewsTags == null) {
                    Logger.error("[NOTIFICATIONS] There is new News Tag! " + newsTag.getDisplayName());
                    continue;
                }

                Logger.debug("[NOTIFICATIONS] Fetching " + newsTag.getDisplayName() + " news...");
                newsTag.fetchNews().execute().whenComplete(((lostArkNews, throwable1) -> {
                    if (throwable1 != null) {
                        throwable1.printStackTrace();
                        Logger.error("[NOTIFICATIONS] There was an exception while getting News from News Tag " + newsTag.getDisplayName() + "!");
                        return;
                    }

                    news.put(staticNewsTags, lostArkNews);
                    Logger.debug("[NOTIFICATIONS] News for News Tag " + newsTag.getDisplayName() + " were loaded.");
                }));
            }
        });

        Logger.debug("[NOTIFICATIONS] Fetching forum categories...");
        lostArkFetcher.fetchForumCategories().execute().whenComplete(((lostArkForumCategories, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                Logger.error("[NOTIFICATIONS] There was an exception while getting forum categories!");
                return;
            }

            Arrays.stream(lostArkForumCategories.getCategoryList().getCategories()).forEach(category -> {
                AtomicReference<WrappedForumCategoryName> wrappedForumCategoryName = new AtomicReference<>();

                Logger.debug("[NOTIFICATIONS] Fetching Forum with category ID " + category.getId() + "...");
                lostArkFetcher.fetchForum(category.getId()).execute().whenComplete((lostArkForum, throwable1) -> {
                    if (throwable1 != null) {
                        throwable1.printStackTrace();
                        Logger.error("[NOTIFICATIONS] There was an exception while getting forum category " + category.getId() + "!");
                        return;
                    }

                    wrappedForumCategoryName.set(new WrappedForumCategoryName(category.getId(), lostArkForum));
                    forums.put(category.getId(), lostArkForum);
                });

                Arrays.stream(category.getSubcategoryIds()).forEach(forumCategoryId -> {
                    Logger.debug("[NOTIFICATIONS] Fetching Forum with category ID " + forumCategoryId + "...");
                    lostArkFetcher.fetchForum(forumCategoryId).execute().whenComplete((lostArkForum, throwable1) -> {
                        if (throwable1 != null) {
                            throwable1.printStackTrace();
                            Logger.error("[NOTIFICATIONS] There was an exception while getting forum sub category " + forumCategoryId + "!");
                            return;
                        }

                        wrappedForumCategoryName.get().addSubcategory(forumCategoryId, lostArkForum);
                        forums.put(forumCategoryId, lostArkForum);
                    });
                });

                synchronized (forumCategoryNames) {
                    forumCategoryNames.add(wrappedForumCategoryName.get());
                }
            });
        }));

        Logger.success("[NOTIFICATIONS] News and Forums fetching done in " + ((System.currentTimeMillis() - start) / 1000) + " seconds!");
    }

    public static Notifications getUnreadNotifications() {
        Notifications notifications = new Notifications();

        Logger.debug("[NOTIFICATIONS] Checking for unread News...");
        news.forEach(((staticNewsTags, lostArkNewsArray) -> {
            Arrays.stream(lostArkNewsArray).forEach(lostArkNews -> {
                notifications.add(getUnreadNotifications(lostArkNews));
            });
        }));

        Logger.debug("[NOTIFICATIONS] Checking for unread Forum topics...");
        forums.forEach((forumCategoryId, lostArkForum) -> {
            notifications.add(getUnreadNotifications(forumCategoryId, lostArkForum));
        });

        return notifications;
    }

    public static Notifications getUnreadNotifications(LostArkNews lostArkNews) {
        Notifications notifications = new Notifications();

        Hashable hashable = Hashable.create(lostArkNews);

        if (HashCache.wasSent(hashable) == Result.FALSE) {
            notifications.addNews(lostArkNews);
            HashCache.setAsSent(hashable);
        }

        return notifications;
    }

    public static Notifications getUnreadNotifications(int forumCategoryId, LostArkForum lostArkForum) {
        Notifications notifications = new Notifications();

        try {
            String forumName = LostArkUtils.getForumCategoryName(forumCategoryId, lostArkForum);

            for (LostArkForum.TopicList.Topic forumTopic : lostArkForum.getTopicList().getTopics()) {
                Hashable hashable = Hashable.create(forumTopic);

                if (HashCache.wasSent(hashable) == Result.FALSE) {
                    try {
                        Logger.debug("[NOTIFICATIONS] Fetching Forum topic from forum ID " + forumCategoryId + " (" + forumTopic.getTitle() + ")...");
                        notifications.addForumTopic(new WrappedForumTopic(forumCategoryId, forumName, forumTopic));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Logger.error("[NOTIFICATIONS] Exception occurred while fetching forum post from forum ID " + forumCategoryId + "!");
                    }
                    HashCache.setAsSent(hashable);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("[NOTIFICATIONS] Exception occurred while getting unread notifications from forum ID " + forumCategoryId + "!");
        }

        return notifications;
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
