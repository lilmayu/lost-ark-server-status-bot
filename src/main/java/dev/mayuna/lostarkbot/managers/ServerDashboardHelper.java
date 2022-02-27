package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.EmbedUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.LostArk;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerDashboardHelper {

    // TODO: Přesunout ten loading starých věcí do jiné classky
    // TODO: Přepsat sem všechny možnost nastavení dashboardy? Že by se to tady hned ukládalo a pro příkazy by to byl takový "compability layer"

    public static final String DATA_FILE = "./server_dashboards.json";
    private static @Getter LostArkServers lostArkServersCache;
    private static @Getter String onlinePlayersCache;

    public static void startDashboardUpdateTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("LostArkServersCacheWorker");

                List<GuildData> guilds = DataManager.getGuilds();

                Logger.debug("Updating " + guilds.size() + " guilds...");

                try {
                    updateCache();

                    long start = System.currentTimeMillis();
                    updateAll();
                    long took = System.currentTimeMillis() - start;

                    Logger.debug("Queuing dashboard updates for " + guilds.size() + " guilds took " + took + "ms " + (guilds.size() != 0 ? "(avg. " + (took / guilds.size()) + "ms)" : ""));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Exception occurred while refreshing cache!");
                }
            }
        }, 1000, 60000 * 5);
    }

    public static void updateCache() throws IOException {
        lostArkServersCache = LostArk.fetchServers();
        onlinePlayersCache = Utils.getOnlinePlayers();
    }

    /**
     * Gets {@link ServerDashboard} from loaded dashboards
     *
     * @param textChannel Channel
     *
     * @return Null if {@link ServerDashboard} does not exists in specified Text Channel
     */
    public static ServerDashboard getServerDashboardByChannel(TextChannel textChannel) {
        GuildData guildData = DataManager.getOrCreateGuildData(textChannel.getGuild());

        synchronized (guildData) {
            for (ServerDashboard serverDashboard : guildData.getServerDashboards()) {
                if (serverDashboard.getManagedGuildMessage().getTextChannel().getIdLong() == textChannel.getIdLong()) {
                    return serverDashboard;
                }
            }

            return null;
        }
    }

    /**
     * Determines if {@link ServerDashboard} exists in specified Text Channel
     *
     * @param textChannel Channel
     *
     * @return true if is, false otherwise
     */
    public static boolean isServerDashboardInChannel(TextChannel textChannel) {
        return getServerDashboardByChannel(textChannel) != null;
    }

    /**
     * Tries to create {@link ServerDashboard} in specified Text Channel
     *
     * @param textChannel Channel
     *
     * @return Null if there already is some {@link ServerDashboard} in specified server or if bot was unable to create/edit message
     */
    public static ServerDashboard createServerDashboard(TextChannel textChannel) {
        if (isServerDashboardInChannel(textChannel)) {
            return null;
        }

        GuildData guildData = DataManager.getOrCreateGuildData(textChannel.getGuild());
        ManagedGuildMessage managedGuildMessage = ManagedGuildMessage.create(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel, null);
        ServerDashboard serverDashboard = new ServerDashboard(managedGuildMessage);

        if (update(serverDashboard)) {
            guildData.addServerDashboard(serverDashboard);
            guildData.save();
            return serverDashboard;
        }

        return null;
    }

    /**
     * Tries to delete {@link ServerDashboard} from specified Text Channel
     *
     * @param textChannel Channel
     *
     * @return True if {@link ServerDashboard} was successfully removed
     */
    public static boolean deleteServerDashboard(TextChannel textChannel) {
        GuildData guildData = DataManager.getOrCreateGuildData(textChannel.getGuild());
        ServerDashboard serverDashboard = getServerDashboardByChannel(textChannel);

        if (serverDashboard == null) {
            return false;
        }

        guildData.removeServerDashboard(serverDashboard);
        guildData.save();

        ManagedGuildMessage managedGuildMessage = serverDashboard.getManagedGuildMessage();
        managedGuildMessage.updateEntries(Main.getJda());

        try {
            managedGuildMessage.getMessage().delete().complete();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Dashboard " + serverDashboard.getName() + "! Probably user deleted guild/Text Channel or bot does not have permissions. However, it was removed from internal cache.");
            return false;
        }

        return true;
    }

    /**
     * Updates message in {@link ServerDashboard}
     *
     * @param serverDashboard {@link ServerDashboard}
     *
     * @return True if message was successfully sent/edited
     */
    public static boolean update(ServerDashboard serverDashboard) {
        ManagedGuildMessage managedGuildMessage = serverDashboard.getManagedGuildMessage();
        Message message = new MessageBuilder().setEmbeds(EmbedUtils.createEmbed(serverDashboard, lostArkServersCache).build()).build();

        try {
            managedGuildMessage.updateEntries(Main.getJda());

            managedGuildMessage.sendOrEditMessage(message, RestActionMethod.QUEUE, success -> {
                Logger.debug("Successfully updated dashboard " + serverDashboard.getName() + " with result " + success);
            }, exception -> {
                exception.printStackTrace();
                Logger.warn("Dashboard " + serverDashboard.getName() + " resulted in exception while updating!");
            });

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Dashboard " + serverDashboard.getName() + "! Probably user removed channel/kicked bot or bot does not have permissions.");
            return false;
        }
    }

    public static void updateAll() {
        synchronized (DataManager.LOCK) {
            for (GuildData guildData : DataManager.getGuilds()) {
                for (ServerDashboard dashboard : guildData.getServerDashboards()) {
                    update(dashboard);
                }
            }
        }
    }

    @Deprecated
    public static void load() {
        Logger.info("Loading old Server Dashboards...");
        List<ServerDashboard> dashboards = new LinkedList<>();

        int index = 0;
        try {
            if (!new File(DATA_FILE).exists()) {
                Logger.debug("No old dashboards found.");
                return;
            }

            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(DATA_FILE);
            JsonArray jsonArray = mayuJson.getOrCreate("serverDashboards", new JsonArray()).getAsJsonArray();
            Gson gson = Utils.getGson();

            for (JsonElement jsonElement : jsonArray) {
                AtomicBoolean successful = new AtomicBoolean(false);
                CountDownLatch finished = new CountDownLatch(1);

                try {
                    AtomicBoolean canContinue = new AtomicBoolean(false);
                    ServerDashboard serverDashboard = gson.fromJson(jsonElement, ServerDashboard.class);
                    String name = serverDashboard.getName();

                    do {
                        serverDashboard.getManagedGuildMessage().updateEntries(Main.getJda(), RestActionMethod.COMPLETE, success -> {
                            Logger.debug("Successfully loaded Server Dashboard " + name);

                            successful.set(true);
                            canContinue.set(true);
                            finished.countDown();
                        }, failure -> {
                            if (failure instanceof NonDiscordException nonDiscordException) {
                                nonDiscordException.printStackTrace();
                                Logger.error("Non-Discord exception occurred while updating entries in Server Dashboard " + name + "! Waiting 1000ms and retrying.");

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException interruptedException) {
                                    throw new RuntimeException("InterruptedException occurred while sleeping!", interruptedException);
                                }
                            } else {
                                failure.printStackTrace();
                                Logger.warn("Unable to load Server Dashboard with name " + name + "! Probably bot was kicked or text channel was deleted.");

                                finished.countDown();
                                canContinue.set(true);
                            }
                        });
                    } while (!canContinue.get());

                    finished.await();

                    if (successful.get()) {
                        dashboards.add(serverDashboard);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.warn("Unable to load JSON Server Dashboard with index " + index + "!");
                }

                index++;
            }

            if (dashboards.size() == 0) {
                Logger.debug("No old dashboards found.");

                Logger.info("Deleting old dashboards file...");
                new File(DATA_FILE).delete();
                return;
            }

            for (ServerDashboard serverDashboard : dashboards) {
                GuildData guildData = DataManager.getOrCreateGuildData(serverDashboard.getManagedGuildMessage().getGuild());
                guildData.addServerDashboard(serverDashboard);
            }

            Logger.info("Deleting old dashboards file...");
            new File(DATA_FILE).delete();

            Logger.success("Successfully loaded " + dashboards.size() + " old Server Dashboards into guilds!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Failed to load Server Dashboards!");
        }
    }
}
