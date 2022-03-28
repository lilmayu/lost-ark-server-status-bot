package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.objects.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.Notifications;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayusjsonutils.JsonUtil;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GuildDataManager {

    private static final @Getter List<GuildData> loadedGuildDataList = Collections.synchronizedList(new LinkedList<>());

    // Getters

    /**
     * Gets {@link GuildData} by guild's ID
     *
     * @param guildID Guild's ID
     *
     * @return Nullable {@link GuildData} (null if not found)
     */
    public static GuildData getGuildData(long guildID) {
        synchronized (loadedGuildDataList) {
            return loadedGuildDataList.stream().filter(guildData -> guildData.getRawGuildID() == guildID).findFirst().orElse(null);
        }
    }

    /**
     * Gets {@link GuildData} by {@link Guild}
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Nullable {@link GuildData} (null if not found)
     */
    public static GuildData getGuildData(@NonNull Guild guild) {
        return getGuildData(guild.getIdLong());
    }

    /**
     * Gets {@link GuildData} by guild's ID, if it does not exist, it creates a new {@link GuildData}
     *
     * @param guildID Guild's ID
     *
     * @return Non-null {@link GuildData}
     */
    public static @NonNull GuildData getOrCreateGuildData(long guildID) {
        GuildData guildData = getGuildData(guildID);

        if (guildData == null) {
            Logger.debug("Creating GuildData for guild " + guildID);
            guildData = new GuildData(guildID);
            guildData.updateEntries(Main.getJda());

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
            guildData.updateEntries(Main.getJda()); // Optimized: this should just return, without any action

            synchronized (loadedGuildDataList) {
                loadedGuildDataList.add(guildData);
            }
        }

        return guildData;
    }

    // Updating

    /**
     * Updates specified {@link GuildData}
     *
     * @param guildData Non-null {@link GuildData}
     */
    public static void updateGuildData(@NonNull GuildData guildData) {
        guildData.updateDashboards();
    }

    /**
     * Updates all loaded {@link GuildData}
     */
    public static void updateAllGuildData() {
        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
            while (guildDataIterator.hasNext()) {
                updateGuildData(guildDataIterator.next());
            }
        }
    }

    // Loading

    /**
     * Loads {@link GuildData} from {@link File}
     *
     * @param file Non-null {@link File}
     *
     * @return Nullable {@link GuildData} (if it fails to load)
     */
    public static GuildData loadGuildData(@NonNull File file) {
        try {
            if (!file.exists()) {
                throw new IOException("File " + file.getName() + " does not exist!");
            }
            return Utils.getGson().fromJson(JsonUtil.createOrLoadJsonFromFile(file).getJsonObject(), GuildData.class);
        } catch (Exception exception) {
            exception.printStackTrace();
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
        synchronized (loadedGuildDataList) {
            loadedGuildDataList.remove(guildData);
            loadedGuildDataList.add(guildData);
        }
        return guildData;
    }

    /**
     * Removes specified guild via ID from loaded {@link GuildData}
     *
     * @param guildID Guild's ID
     */
    public static void removeGuildData(long guildID) {
        synchronized (loadedGuildDataList) {
            loadedGuildDataList.removeIf(guildData -> guildData.getRawGuildID() == guildID);
        }
    }

    /**
     * Deletes specified guild via ID from data storage. {@link GuildDataManager#removeGuildData(long)} should be called before this method
     *
     * @param guildID Guild's ID
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
    public static boolean loadAll() {
        synchronized (loadedGuildDataList) {
            loadedGuildDataList.clear();
        }

        Logger.info("[GUILD-LOAD] Loading GuildData...");
        long start = System.currentTimeMillis();

        Logger.debug("[GUILD-LOAD] Checking for folders...");
        File guildsFolder = checkFolders();
        if (guildsFolder == null) {
            Logger.error("[GUILD-LOAD] Failed to create folder for GuildData! (" + Constants.GUILDS_FOLDER + ")!");
            return false;
        }

        File[] guildsFiles = guildsFolder.listFiles();
        if (guildsFiles == null) {
            Logger.error("[GUILD-LOAD] Unable to list files in " + Constants.GUILDS_FOLDER + " folder!");
            return false;
        }

        Logger.debug("[GUILD-LOAD] Loading...");
        for (File file : guildsFiles) {
            GuildData guildData = loadGuildData(file);

            if (guildData != null) {
                try {
                    guildData.updateEntries(Main.getJda());
                    Logger.debug("[GUILD-LOAD] Successfully loaded GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");

                    synchronized (loadedGuildDataList) {
                        loadedGuildDataList.add(guildData);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.warn("[GUILD-LOAD] Unable to update entries in GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")! Probably bot is not in this guild anymore.");
                }
            } else {
                Logger.warn("[GUILD-LOAD] Unable to load GuildData from file '" + file.getName() + "'!");
            }
        }

        Logger.success("[GUILD-LOAD] Loading completed, loaded " + loadedGuildDataList.size() + " out of " + guildsFiles.length + " files. (took " + (System.currentTimeMillis() - start) + "ms)");
        return true;
    }

    /**
     * Loads all {@link GuildData}'s dashboards and notification channels
     */
    public static void loadAllGuildData() {
        Logger.info("[GUILD-LOAD] Loading all dashboards and notification channels...");
        long start = System.currentTimeMillis();

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();

            while (guildDataIterator.hasNext()) {
                GuildData guildData = guildDataIterator.next();

                Logger.debug("[GUILD-LOAD] Loading dashboards for GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                guildData.loadDashboards();

                Logger.debug("[GUILD-LOAD] Loading notification channels for GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                guildData.loadNotificationChannels();
            }
        }

        Logger.success("[GUILD-LOAD] Loading dashboards and notification channels completed, loaded " + countAllDashboards() + " dashboards and " + countAllNotificationChannels() + " notification channels. (took " + (System.currentTimeMillis() - start) + "ms)");
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
            exception.printStackTrace();
            Logger.error("Exception occurred while saving GuildData " + guildData.getRawGuildID() + "(" + guildData.getName() + ")!");
            return false;
        }
    }

    /**
     * Saves all currently loaded {@link GuildData} into their files
     */
    public static void saveAll() {
        Logger.info("[GUILD-SAVE] Saving " + loadedGuildDataList.size() + " GuildData...");
        long start = System.currentTimeMillis();

        Logger.debug("[GUILD-SAVE] Checking for folders...");
        File guildsFolder = checkFolders();
        if (guildsFolder == null) {
            Logger.error("[GUILD-SAVE] Failed to create folder for GuildData! (" + Constants.GUILDS_FOLDER + ")!");
            return;
        }

        Logger.debug("[GUILD-SAVE] Saving...");
        int successfullySaved = 0;
        for (GuildData guildData : loadedGuildDataList) {
            if (saveGuildData(guildData)) {
                successfullySaved++;
                Logger.debug("[GUILD-SAVE] Successfully saved GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
            } else {
                Logger.warn("[GUILD-SAVE] Unable to save GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")!");
            }
        }

        Logger.success("[GUILD-SAVE] Saving completed, saved " + successfullySaved + " out of " + loadedGuildDataList.size() + " GuildData. (took " + (System.currentTimeMillis() - start) + "ms)");
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

    /**
     * Counts all dashboards
     *
     * @return Number of dashboards
     */
    public static int countAllDashboards() {
        int counter = 0;

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
            while (guildDataIterator.hasNext()) {
                counter += guildDataIterator.next().getLoadedServerDashboards().size();
            }
        }

        return counter;
    }

    /**
     * Counts all notification channels
     *
     * @return Number of notification channels
     */
    public static int countAllNotificationChannels() {
        int counter = 0;

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
            while (guildDataIterator.hasNext()) {
                counter += guildDataIterator.next().getLoadedNotificationChannels().size();
            }
        }

        return counter;
    }

    public static void processAllGuildDataWithNotifications(Notifications notifications) {
        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().sendUnreadNotificationsByRules(notifications);
            }
        }
    }

    public static void processServerStatusChange(LostArkServers previous, LostArkServers current) {
        LostArkServersChange lostArkServersChange = new LostArkServersChange(previous, current);

        if (!lostArkServersChange.hasChangedAnything()) {
            return;
        }

        synchronized (loadedGuildDataList) {
            Iterator<GuildData> guildDataIterator = loadedGuildDataList.listIterator();
            while (guildDataIterator.hasNext()) {
                guildDataIterator.next().processServerStatusChange(lostArkServersChange);
            }
        }
    }
}
