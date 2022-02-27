package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataManager {

    public static final String GUILDS_FOLDER = "./guilds/";
    public static final Object LOCK = new Object();
    private static final @Getter List<GuildData> guilds = new LinkedList<>();

    public static GuildData getOrCreateGuildData(long guildID) {
        GuildData guildData = getGuildData(guildID);

        if (guildData == null) {
            Logger.debug("Creating guild data for guild with ID " + guildID);
            guildData = new GuildData(guildID);
            guildData.updateEntries(Main.getJda());
            guilds.add(guildData);
        }

        return guildData;
    }

    public static GuildData getOrCreateGuildData(Guild guild) {
        GuildData guildData = getGuildData(guild);

        if (guildData == null) {
            Logger.debug("Creating guild data for guild with ID " + guild.getIdLong());
            guildData = new GuildData(guild);
            guildData.updateEntries(Main.getJda());
            guilds.add(guildData);
        }

        return guildData;
    }

    public static GuildData getGuildData(long guildID) {
        synchronized (LOCK) {
            for (GuildData guildData : guilds) {
                if (guildData.getRawGuildID() == guildID) {
                    return guildData;
                }
            }

            return null;
        }
    }

    public static GuildData getGuildData(Guild guild) {
        return getGuildData(guild.getIdLong());
    }

    public static int getDashboardCount() {
        int dashboards = 0;

        for (GuildData guildData : guilds) {
            dashboards += guildData.getServerDashboards().size();
        }

        return dashboards;
    }

    public static boolean load() {
        guilds.clear();
        Logger.info("[LOAD-DATA] Phase 1/3: Checking folders...");

        File guildsFolder = new File(GUILDS_FOLDER);
        if (!guildsFolder.exists()) {
            if (!guildsFolder.mkdirs()) {
                Logger.error("[LOAD-DATA] Could not create guilds folder!");
                return false;
            }
        }

        File[] guildsFiles = guildsFolder.listFiles();

        if (guildsFiles == null) {
            Logger.error("[LOAD-DATA] Unable to list files in guilds folder!");
            return false;
        }

        Logger.info("[LOAD-DATA] Phase 2/3: Loading guilds...");

        Gson gson = Utils.getGson();

        List<GuildData> loadingGuilds = new LinkedList<>();
        for (File guildFile : guildsFiles) {
            try {
                MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(guildFile);
                JsonObject jsonObject = mayuJson.getJsonObject();

                GuildData guildData = gson.fromJson(jsonObject, GuildData.class);

                loadingGuilds.add(guildData);
                Logger.debug("[LOAD-DATA] Loaded guild data " + guildData.getName());
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.error("[LOAD-DATA] Could not guild file: " + guildFile.getName());
            }
        }

        Logger.debug("[LOAD-DATA] Loaded " + loadingGuilds.size() + " guilds (" + guildsFiles.length + " files)");
        Logger.info("[LOAD-DATA] Phase 3/3: Loading guild's dashboards...");

        List<GuildData> successfullyLoadedGuilds = new LinkedList<>();
        for (GuildData guildData : loadingGuilds) {
            try {
                List<ServerDashboard> serverDashboards = guildData.getServerDashboards();

                for (ServerDashboard serverDashboard : serverDashboards) {
                    CountDownLatch finished = new CountDownLatch(1);
                    AtomicBoolean canContinue = new AtomicBoolean(false);

                    do {
                        serverDashboard.getManagedGuildMessage().updateEntries(Main.getJda(), RestActionMethod.COMPLETE, success -> {
                            Logger.debug("[LOAD-DATA] Successfully loaded Server Dashboard " + serverDashboard.getName());

                            canContinue.set(true);
                            finished.countDown();
                        }, failure -> {
                            if (failure instanceof NonDiscordException nonDiscordException) {
                                nonDiscordException.printStackTrace();
                                Logger.error("[LOAD-DATA] Non-Discord exception occurred while updating entries in Server Dashboard " + serverDashboard.getName() + "! Waiting 1000ms and retrying.");

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException interruptedException) {
                                    throw new RuntimeException("[LOAD-DATA] InterruptedException occurred while sleeping!", interruptedException);
                                }
                            } else {
                                failure.printStackTrace();
                                Logger.warn("[LOAD-DATA] Unable to load Server Dashboard with name " + serverDashboard.getName() + "! Probably bot was kicked or text channel was deleted.");

                                finished.countDown();
                                canContinue.set(true);
                            }
                        });
                    } while (!canContinue.get());

                    finished.await();
                }

                successfullyLoadedGuilds.add(guildData);
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.error("[LOAD-DATA] Could not load server dashboards for guild '" + guildData.getName() + "' (" + guildData.getRawGuildID() + ")!");
            }
        }

        guilds.addAll(successfullyLoadedGuilds);
        Logger.debug("[LOAD-DATA] Successfully loaded guilds: " + successfullyLoadedGuilds.size() + " (" + loadingGuilds.size() + " loading guilds)");

        Logger.success("[LOAD-DATA] Successfully loaded " + guilds.size() + " guilds!");
        return true;
    }

    public static boolean saveAll() {
        Logger.info("[SAVE-DATA] Phase 1/2: Checking folders...");

        File guildsFolder = new File(GUILDS_FOLDER);
        if (!guildsFolder.exists()) {
            if (!guildsFolder.mkdirs()) {
                Logger.error("[DATA] Could not create guilds folder!");
                return false;
            }
        }

        Logger.info("[SAVE-DATA] Phase 2/2: Saving guilds into files...");

        int saved = 0;
        for (GuildData guildData : guilds) {
            if (save(guildData)) {
                saved++;
            }
        }

        Logger.success("[SAVE-DATA] Successfully saved " + saved + " guilds!");
        return true;
    }

    public static boolean save(GuildData guildData) {
        try {
            JsonObject jsonObject = Utils.getGson().toJsonTree(guildData).getAsJsonObject();
            JsonUtil.saveJson(jsonObject, new File(GUILDS_FOLDER + guildData.getRawGuildID() + ".json"));

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("[SAVE-GUILD-DATA] Could not save guild data '" + guildData.getName() + "' (" + guildData.getRawGuildID() + ")!");

            return false;
        }
    }
}
