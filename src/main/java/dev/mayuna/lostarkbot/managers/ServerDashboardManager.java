package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.Main;
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
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerDashboardManager {

    public static final String DATA_FILE = "./server_dashboards.json";
    private final static @Getter List<ServerDashboard> dashboards = new ArrayList<>();
    private static @Getter LostArkServers lostArkServersCache;
    private static @Getter String onlinePlayersCache;

    public static void init() {
        Logger.debug("Creating timer with 5 minute delay...");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("LostArkServersCacheWorker");
                Logger.debug("Updating " + dashboards.size() + " server dashboards...");

                try {
                    updateCache();

                    long start = System.currentTimeMillis();
                    updateAll();
                    long took = System.currentTimeMillis() - start;

                    Logger.debug("Queuing updates for " + dashboards.size() + " server dashboards took " + took + "ms " + (dashboards.size() != 0 ? "(avg. " + (took / dashboards.size()) + "ms)" : ""));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.error("Exception occurred while refreshing cache!");
                }
            }
        }, 60000 * 5, 60000 * 5);
    }

    public static void updateCache() throws IOException {
        lostArkServersCache = LostArk.fetchServers();
        onlinePlayersCache = Utils.getOnlinePlayers();
    }

    /**
     * Gets {@link ServerDashboard} from loaded dashboards
     *
     * @param channel Channel
     *
     * @return Null if {@link ServerDashboard} does not exists in specified channel
     */
    public static ServerDashboard getServerDashboardByChannel(AbstractChannel channel) {
        synchronized (dashboards) {
            for (ServerDashboard serverDashboard : dashboards) {
                if (serverDashboard.getManagedGuildMessage().getTextChannel().getIdLong() == channel.getIdLong()) {
                    return serverDashboard;
                }
            }

            return null;
        }
    }

    /**
     * Determines if {@link ServerDashboard} exists in specified channel
     *
     * @param channel Channel
     *
     * @return true if is, false otherwise
     */
    public static boolean isServerDashboardInChannel(AbstractChannel channel) {
        return getServerDashboardByChannel(channel) != null;
    }

    /**
     * Tries to create {@link ServerDashboard} in specified channel
     *
     * @param textChannel Channel
     *
     * @return Null if there already is some {@link ServerDashboard} in specified server or if bot was unable to create/edit message
     */
    public static ServerDashboard createServerDashboard(TextChannel textChannel) {
        if (isServerDashboardInChannel(textChannel)) {
            return null;
        }

        ManagedGuildMessage managedGuildMessage = ManagedGuildMessage.create(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel, null);
        ServerDashboard serverDashboard = new ServerDashboard(managedGuildMessage);

        if (update(serverDashboard)) {
            dashboards.add(serverDashboard);
            return serverDashboard;
        }

        return null;
    }

    /**
     * Tries to delete {@link ServerDashboard} from specified channel
     *
     * @param channel Channel
     *
     * @return True if {@link ServerDashboard} was successfully removed
     */
    public static boolean deleteServerDashboard(AbstractChannel channel) {
        ServerDashboard serverDashboard = getServerDashboardByChannel(channel);

        if (serverDashboard == null) {
            return false;
        }

        synchronized (dashboards) {
            dashboards.remove(serverDashboard);
        }

        ManagedGuildMessage managedGuildMessage = serverDashboard.getManagedGuildMessage();
        managedGuildMessage.updateEntries(Main.getJda());

        try {
            managedGuildMessage.getMessage().delete().complete();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Dashboard " + serverDashboard.getName() + "! Probably user deleted guild/channel or bot does not have permissions. However, it was removed from internal cache.");
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
        synchronized (dashboards) {
            //List<ServerDashboard> dashboardsToRemove = new ArrayList<>();

            for (ServerDashboard serverDashboard : dashboards) {
                update(serverDashboard);
                /* Disabled due a bug
                if (!update(serverDashboard)) {
                    dashboardsToRemove.add(serverDashboard);
                }*/
            }

            // Disabled due a bug
            //dashboards.removeAll(dashboardsToRemove);
        }
    }

    public static void load() {
        Logger.info("Loading Server Dashboards...");
        dashboards.clear();

        int index = 0;
        try {
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

            Logger.success("Successfully loaded " + dashboards.size() + " Server Dashboards!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Failed to load Server Dashboards!");
        }
    }

    public static void save() {
        Logger.info("Saving Server Dashboards...");
        Gson gson = Utils.getGson();

        JsonArray jsonArray = new JsonArray();
        try {
            JsonObject jsonObject = new JsonObject();

            jsonArray = new JsonArray();
            for (ServerDashboard serverDashboard : dashboards) {
                jsonArray.add(gson.toJsonTree(serverDashboard));
            }
            jsonObject.add("serverDashboards", jsonArray);

            JsonUtil.saveJson(jsonObject, new File(DATA_FILE));
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Failed to save Server Dashboards!");
        }

        Logger.success("Successfully saved " + jsonArray.size() + " Server Dashboards!");
    }
}
