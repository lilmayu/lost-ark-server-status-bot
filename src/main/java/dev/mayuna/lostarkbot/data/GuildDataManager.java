package dev.mayuna.lostarkbot.data;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.data.loaders.GuildDataLoader;
import dev.mayuna.lostarkbot.objects.other.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.other.MayuTweet;
import dev.mayuna.lostarkbot.objects.other.Notifications;
import dev.mayuna.lostarkbot.objects.features.GuildData;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayusjsonutils.JsonUtil;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildDataManager {

    private static Map<Integer, List<GuildData>> loadedGuildDataMap;

    public static void init(int totalShards) {
        Logger.debug("Initializing GuildData storage...");

        loadedGuildDataMap = Collections.synchronizedMap(new HashMap<>(totalShards));

        for (int x = 0; x < totalShards; x++) {
            loadedGuildDataMap.put(x, Collections.synchronizedList(new LinkedList<>()));
        }

        Logger.info("Initialized GuildData storage for " + totalShards + " shards.");
    }

    // Getters

    public static List<GuildData> getLoadedGuildDataListByShard(int shardId) {
        synchronized (loadedGuildDataMap) {
            if (shardId >= 0 && shardId < loadedGuildDataMap.size()) {
                return loadedGuildDataMap.get(shardId);
            }
        }

        return null;
    }

    /**
     * Gets {@link GuildData} by guild's id
     *
     * @param guildID Guild's id
     *
     * @return Nullable {@link GuildData} (null if not found)
     */
    public static GuildData getGuildData(long guildID) {
        synchronized (loadedGuildDataMap) {
            for (Map.Entry<Integer, List<GuildData>> entry : loadedGuildDataMap.entrySet()) {
                GuildData guildData = entry.getValue().stream().filter(guildDataStreamed -> guildDataStreamed.getRawGuildID() == guildID).findFirst().orElse(null);

                if (guildData != null) {
                    return guildData;
                }
            }
        }

        return null;
    }

    /**
     * Gets {@link GuildData} by {@link Guild}
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Nullable {@link GuildData} (null if not found)
     */
    public static GuildData getGuildData(@NonNull Guild guild) {
        var loadedGuildDataList = getLoadedGuildDataListByShard(guild.getJDA().getShardInfo().getShardId());

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method get: " + guild.getJDA()
                    .getShardInfo()
                    .getShardId() + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return null;
        }

        synchronized (loadedGuildDataList) {
            return loadedGuildDataList.stream().filter(guildDataStreamed -> guildDataStreamed.getRawGuildID() == guild.getIdLong()).findFirst().orElse(null);
        }
    }

    /**
     * Gets {@link GuildData} by guild's id, if it does not exist, it creates a new {@link GuildData}
     *
     * @param guildID Guild's id
     *
     * @return Non-null {@link GuildData}
     */
    public static @NonNull GuildData getOrCreateGuildData(long guildID) {
        GuildData guildData = getGuildData(guildID);

        if (guildData == null) {
            Logger.debug("Creating GuildData for guild " + guildID);
            guildData = new GuildData(guildID);
            guildData.updateEntries(Main.getMayuShardManager().get());

            if (guildData.getShardId() == -1) {
                Logger.error("GuildData has -1 ShardId even after updating it's entries! Putting it into ShardId 0 batch...");
                var loadedGuildDataList = getLoadedGuildDataListByShard(0);

                synchronized (loadedGuildDataList) {
                    loadedGuildDataList.add(guildData);
                }

                return guildData;
            }

            var loadedGuildDataList = getLoadedGuildDataListByShard(guildData.getShardId());

            if (loadedGuildDataList == null) {
                Logger.error("Incorrect ShardId in core method getOrCreate(long): " + guildData.getGuild()
                        .getJDA()
                        .getShardInfo()
                        .getShardId() + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
                Logger.error("Putting it into ShardId 0 batch...");

                loadedGuildDataList = getLoadedGuildDataListByShard(0);

                synchronized (loadedGuildDataList) {
                    loadedGuildDataList.add(guildData);
                }

                return guildData;
            }

            synchronized (loadedGuildDataList) {
                loadedGuildDataList.add(guildData);
            }
        }

        return guildData;
    }

    /**
     * Gets {@link GuildData} by {@link Guild}, if it does not exist, it creates a new {@link GuildData}
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link GuildData}
     */
    public static @NonNull GuildData getOrCreateGuildData(@NonNull Guild guild) {
        GuildData guildData = getGuildData(guild);

        if (guildData == null) {
            Logger.debug("Creating GuildData for guild " + guild.getIdLong());
            guildData = new GuildData(guild);
            guildData.updateEntries(Main.getMayuShardManager().get()); // TODO: Optimized: this should just return, without any action

            if (guildData.getShardId() == -1) {
                Logger.error("GuildData has -1 ShardId even after updating it's entries! Putting it into ShardId 0 batch...");
                var loadedGuildDataList = getLoadedGuildDataListByShard(0);

                synchronized (loadedGuildDataList) {
                    loadedGuildDataList.add(guildData);
                }

                return guildData;
            }

            var loadedGuildDataList = getLoadedGuildDataListByShard(guildData.getShardId());

            if (loadedGuildDataList == null) {
                Logger.error("Incorrect ShardId in core method getOrCreate(Guild): " + guildData.getGuild()
                        .getJDA()
                        .getShardInfo()
                        .getShardId() + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
                Logger.error("Putting it into ShardId 0 batch...");

                loadedGuildDataList = getLoadedGuildDataListByShard(0);

                synchronized (loadedGuildDataList) {
                    loadedGuildDataList.add(guildData);
                }

                return guildData;
            }

            synchronized (loadedGuildDataList) {
                loadedGuildDataList.add(guildData);
            }
        }

        return guildData;
    }

    // Updating

    /**
     * Updates all loaded @{@link GuildData} regardless of shard id
     */
    @Deprecated
    public static void updateAllServerDashboards() {
        int size;

        synchronized (loadedGuildDataMap) {
            size = loadedGuildDataMap.size();
        }

        for (int shardId = 0; shardId < size; shardId++) {
            updateAllServerDashboards(shardId);
        }
    }

    /**
     * Updates all loaded {@link GuildData} for specified shard id
     *
     * @param shardId Shard id
     */
    public static void updateAllServerDashboards(int shardId) {
        var loadedGuildDataList = getLoadedGuildDataListByShard(shardId);

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method updateAllServerDashboards: " + shardId + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return;
        }

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.iterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().updateDashboards();
            }
        }
    }

    @Deprecated
    public static void processAllGuildDataWithNotifications(Notifications notifications) {
        int size;

        synchronized (loadedGuildDataMap) {
            size = loadedGuildDataMap.size();
        }

        for (int shardId = 0; shardId < size; shardId++) {
            processAllGuildDataWithNotifications(shardId, notifications);
        }
    }

    public static void processAllGuildDataWithNotifications(int shardId, Notifications notifications) {
        var loadedGuildDataList = getLoadedGuildDataListByShard(shardId);

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method processAllGuildDataWithNotifications: " + shardId + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return;
        }

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.iterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().sendUnreadNotificationsByRules(notifications);
            }
        }
    }

    @Deprecated
    public static void processServerStatusChange(LostArkServers previous, LostArkServers current) {
        int size;

        synchronized (loadedGuildDataMap) {
            size = loadedGuildDataMap.size();
        }

        for (int shardId = 0; shardId < size; shardId++) {
            processServerStatusChange(shardId, previous, current);
        }
    }

    public static void processServerStatusChange(int shardId, LostArkServers previous, LostArkServers current) {
        LostArkServersChange lostArkServersChange = new LostArkServersChange(previous, current);

        if (!lostArkServersChange.hasChangedAnything()) {
            return;
        }

        Logger.flow("[STATUS-CHANGE] Some servers changed their statuses.");

        Logger.info("[STATUS-CHANGE] Sending server status change messages to notification channels...");
        long took;

        var loadedGuildDataList = getLoadedGuildDataListByShard(shardId);

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method processServerStatusChange: " + shardId + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return;
        }

        synchronized (loadedGuildDataList) {
            long start = System.currentTimeMillis();

            Iterator<GuildData> guildDataIterator = loadedGuildDataList.iterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().processServerStatusChange(lostArkServersChange);
            }

            took = System.currentTimeMillis() - start;
        }

        Logger.info("[STATUS-CHANGE] Sending server status change messages done in " + took + "ms");
    }

    @Deprecated
    public static void processMayuTweet(MayuTweet mayuTweet) {
        int size;

        synchronized (loadedGuildDataMap) {
            size = loadedGuildDataMap.size();
        }

        for (int shardId = 0; shardId < size; shardId++) {
            processMayuTweet(shardId, mayuTweet);
        }
    }

    public static void processMayuTweet(int shardId, MayuTweet mayuTweet) {
        Logger.info("[TWITTER] Sending tweet " + mayuTweet.getTweetId() + " to notification channels...");
        long took;

        var loadedGuildDataList = getLoadedGuildDataListByShard(shardId);

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method processMayuTweet: " + shardId + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return;
        }

        synchronized (loadedGuildDataList) {
            long start = System.currentTimeMillis();

            Iterator<GuildData> guildDataIterator = loadedGuildDataList.iterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().processMayuTweet(mayuTweet);
            }

            took = System.currentTimeMillis() - start;
        }

        Logger.info("[TWITTER] Sending tweet done in " + took + "ms");
    }

    // Loading

    /**
     * Loads {@link GuildData} from {@link File}
     *
     * @param file Non-null {@link File}
     *
     * @return Nullable {@link GuildData} (if it fails to load)
     */
    @Deprecated
    public static GuildData loadGuildData(@NonNull File file) {
        try {
            if (!file.exists()) {
                return null;
            }
            return Utils.getGson().fromJson(JsonUtil.createOrLoadJsonFromFile(file).getJsonObject(), GuildData.class);
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Exception occurred while loading GuildData from file '" + file.getName() + "'!");
            return null;
        }
    }

    /**
     * Removes and adds {@link GuildData} from loaded ones
     *
     * @param guildData Non-null {@link GuildData}
     *
     * @return Non-null {@link GuildData}
     */
    public static GuildData addOrReplaceGuildData(@NonNull GuildData guildData) {
        var loadedGuildDataList = getLoadedGuildDataListByShard(guildData.getShardId());

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method addOrReplaceGuildData: " + guildData.getShardId() + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return guildData;
        }

        synchronized (loadedGuildDataList) {
            loadedGuildDataList.remove(guildData);
            loadedGuildDataList.add(guildData);
        }
        return guildData;
    }

    /**
     * Removes specified guild via {@link Guild} object from loaded {@link GuildData}
     *
     * @param guild {@link Guild} object
     */
    public static void removeGuildData(Guild guild) {
        var loadedGuildDataList = getLoadedGuildDataListByShard(guild.getJDA().getShardInfo().getShardId());

        if (loadedGuildDataList == null) {
            Logger.error("Incorrect ShardId in core method removeGuildData: " + guild.getJDA()
                    .getShardInfo()
                    .getShardId() + " (there are only " + loadedGuildDataMap.size() + " map entries!) (list is null)");
            return;
        }

        synchronized (loadedGuildDataList) {
            loadedGuildDataList.removeIf(guildData -> guildData.getRawGuildID() == guild.getIdLong());
        }
    }

    /**
     * Removes specified guild via id from loaded {@link GuildData}
     *
     * @param guildID Guild's id
     */
    public static void removeGuildData(long guildID) {
        GuildData guildData = getGuildData(guildID);

        if (guildData == null) {
            return;
        }

        try {
            guildData.updateEntries();

            removeGuildData(guildData.getGuild());
        } catch (Exception ignored) {
        }
    }

    /**
     * Deletes specified guild via id from data storage. {@link GuildDataManager#removeGuildData(long)} should be called before this method
     *
     * @param guildID Guild's id
     *
     * @return True if deleted, false otherwise
     */
    public static boolean deleteGuildData(long guildID) {
        File file = GuildData.getGuildDataFile(guildID);

        if (file.exists()) {
            return file.delete();
        }

        return false;
    }

    /**
     * Loads all Guilds from data storage
     *
     * @return True if successfully loaded, false otherwise
     */
    public static boolean loadAllFiles() {
        Logger.debug("[GUILD-LOAD] Clearing all previously loaded GuildData...");
        synchronized (loadedGuildDataMap) {
            for (List<GuildData> loadedGuildDataList : loadedGuildDataMap.values()) {
                synchronized (loadedGuildDataList) {
                    loadedGuildDataList.clear();
                }
            }
        }

        Logger.info("[GUILD-LOAD] Loading GuildData...");
        long start = System.currentTimeMillis();

        Logger.flow("[GUILD-LOAD] Checking for folders...");
        File guildsFolder = checkFolders();
        if (guildsFolder == null) {
            Logger.fatal("[GUILD-LOAD] Failed to create folder for GuildData! (" + Constants.GUILDS_FOLDER + ")!");
            return false;
        }

        File[] guildsFiles = guildsFolder.listFiles();
        if (guildsFiles == null) {
            Logger.fatal("[GUILD-LOAD] Unable to list files in " + Constants.GUILDS_FOLDER + " folder!");
            return false;
        }

        Logger.flow("[GUILD-LOAD] Loading...");
        for (File file : guildsFiles) {
            GuildData guildData = GuildDataLoader.loadGuildDataFromFile(file);

            if (guildData == null) {
                Logger.error("[GUILD-LOAD] Unable to load GuildData from file '" + file.getName() + "'!");
                continue;
            }

            if (!GuildDataLoader.updateEntries(guildData)) {
                continue;
            }

            addOrReplaceGuildData(guildData);
        }

        Logger.success("[GUILD-LOAD] Loading completed, loaded " + countGuildDataSize() + " out of " + guildsFiles.length + " files. (took " + (System.currentTimeMillis() - start) + "ms)");
        return true;
    }

    /**
     * Loads all {@link GuildData}'s dashboards and notification channels
     */
    public static void loadAllGuildDataFeatures() {
        Logger.info("[GUILD-LOAD] Loading all features shard by shard...");
        long totalStart = System.currentTimeMillis();

        synchronized (loadedGuildDataMap) {
            loadedGuildDataMap.forEach((shardId, loadedGuildDataList) -> {
                Logger.info("[GUILD-LOAD] Loading GuildData features for Shard id " + shardId + "...");

                synchronized (loadedGuildDataList) {
                    long start = System.currentTimeMillis();
                    int etaCounter = -1;
                    int totalCounter = 0;
                    int guildDataListSize = loadedGuildDataList.size();
                    Iterator<GuildData> guildDataIterator = loadedGuildDataList.iterator();

                    while (guildDataIterator.hasNext()) {
                        GuildData guildData = guildDataIterator.next();

                        long singleStart = 0;
                        etaCounter++;
                        totalCounter++;

                        Logger.flow("[GUILD-LOAD] Loading dashboards for GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                        guildData.loadDashboards();

                        Logger.flow("[GUILD-LOAD] Loading notification channels for GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                        guildData.loadNotificationChannels();

                        long singleElapsedTime = System.currentTimeMillis() - singleStart;
                        Logger.flow("[GUILD-DATA] Loading GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ") took " + singleElapsedTime + "ms");

                        if (etaCounter == 10) {
                            etaCounter = -1;

                            double timePerGuildData = (System.currentTimeMillis() - start) / (double) totalCounter;
                            double timeRemaining = (guildDataListSize - totalCounter) * timePerGuildData;
                            Logger.debug("[GUILD-DATA] Loaded " + totalCounter + "/" + guildDataListSize + " GuildData on Shard id " + shardId + " (ETA: " + Utils.getTimerWithoutMillis((long) Math.ceil(timeRemaining)) + ")");
                        }
                    }
                }
            });
        }

        Logger.success("[GUILD-LOAD] Loading features completed, loaded " + countAllDashboards() + " dashboards and " + countAllNotificationChannels() + " notification channels. (took " + (System.currentTimeMillis() - totalStart) + "ms)");
    }

    // Saving

    /**
     * Saves {@link GuildData}
     *
     * @param guildData Non-null {@link GuildData}
     *
     * @return True if successful, false otherwise
     */
    public static boolean saveGuildData(@NonNull GuildData guildData) {
        try {
            JsonUtil.saveJson(Utils.getGson().toJsonTree(guildData).getAsJsonObject(), GuildData.getGuildDataFile(guildData.getRawGuildID()));
            return true;
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Exception occurred while saving GuildData " + guildData.getRawGuildID() + "(" + guildData.getName() + ")!");
            return false;
        }
    }

    /**
     * Saves all currently loaded {@link GuildData} into their files
     */
    public static void saveAll() {
        Logger.info("[GUILD-SAVE] Saving " + countGuildDataSize() + " GuildData...");
        long start = System.currentTimeMillis();

        Logger.flow("[GUILD-SAVE] Checking for folders...");
        File guildsFolder = checkFolders();
        if (guildsFolder == null) {
            Logger.error("[GUILD-SAVE] Failed to create folder for GuildData! (" + Constants.GUILDS_FOLDER + ")!");
            return;
        }

        Logger.flow("[GUILD-SAVE] Saving...");
        AtomicInteger successfullySaved = new AtomicInteger();

        synchronized (loadedGuildDataMap) {
            loadedGuildDataMap.forEach((shardId, loadedGuildDataList) -> {
                Logger.info("Saving data for GuildData within Shard id " + shardId);

                synchronized (loadedGuildDataList) {
                    for (GuildData guildData : loadedGuildDataList) {
                        if (saveGuildData(guildData)) {
                            successfullySaved.getAndIncrement();
                            Logger.flow("[GUILD-SAVE] Successfully saved GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                        } else {
                            Logger.error("[GUILD-SAVE] Unable to save GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")!");
                        }
                    }
                }
            });
        }

        Logger.success("[GUILD-SAVE] Saving completed, saved " + successfullySaved + " out of " + countGuildDataSize() + " GuildData. (took " + (System.currentTimeMillis() - start) + "ms)");
    }

    // Others

    private static File checkFolders() {
        File guildsFolder = new File(Constants.GUILDS_FOLDER);
        if (!guildsFolder.exists()) {
            if (guildsFolder.mkdirs()) {
                return guildsFolder;
            } else {
                return null;
            }
        }

        return guildsFolder;
    }

    public static int countGuildDataSize() {
        if (loadedGuildDataMap == null) {
            return -1;
        }

        AtomicInteger value = new AtomicInteger();

        synchronized (loadedGuildDataMap) {
            loadedGuildDataMap.forEach((shardId, loadedGuildDataList) -> {
                synchronized (loadedGuildDataList) {
                    value.addAndGet(loadedGuildDataList.size());
                }
            });
        }

        return value.get();
    }

    /**
     * Counts all dashboards
     *
     * @return Number of dashboards
     */
    public static int countAllDashboards() {
        if (loadedGuildDataMap == null) {
            return -1;
        }

        AtomicInteger counter = new AtomicInteger();

        synchronized (loadedGuildDataMap) {
            loadedGuildDataMap.forEach((shardId, loadedGuildDataList) -> {
                synchronized (loadedGuildDataList) {
                    Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
                    while (guildDataIterator.hasNext()) {
                        counter.addAndGet(guildDataIterator.next().getLoadedServerDashboards().size());
                    }
                }

            });
        }

        return counter.get();
    }

    /**
     * Counts all notification channels
     *
     * @return Number of notification channels
     */
    public static int countAllNotificationChannels() {
        if (loadedGuildDataMap == null) {
            return -1;
        }

        AtomicInteger counter = new AtomicInteger();

        synchronized (loadedGuildDataMap) {
            loadedGuildDataMap.forEach((shardId, loadedGuildDataList) -> {
                synchronized (loadedGuildDataList) {
                    Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
                    while (guildDataIterator.hasNext()) {
                        counter.addAndGet(guildDataIterator.next().getLoadedNotificationChannels().size());
                    }
                }
            });
        }

        return counter.get();
    }
}
