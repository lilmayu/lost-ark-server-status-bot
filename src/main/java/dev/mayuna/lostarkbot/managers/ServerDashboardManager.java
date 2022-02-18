package dev.mayuna.lostarkbot.managers;

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
import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerDashboardManager {

    public static final String DATA_FILE = "./server_dashboards.json";
    private final static @Getter List<ServerDashboard> dashboards = new ArrayList<>();
    private static @Getter LostArkServers lostArkServersCache;
    private static @Getter String onlinePlayersCache;

    public static void init() {
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

                    Logger.debug("Updating " + dashboards.size() + " server dashboards took " + took + "ms " + (dashboards.size() != 0 ? "(avg. " + (took / dashboards.size()) + "ms)" : ""));
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
     * @param channel Channel
     *
     * @return Null if {@link ServerDashboard} does not exists in specified channel
     */
    public static ServerDashboard getServerDashboardByChannel(AbstractChannel channel) {
        synchronized (dashboards) {
            for (ServerDashboard serverDashboard : dashboards) {
                if (serverDashboard.getManagedMessage().getMessageChannel().getIdLong() == channel.getIdLong()) {
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

        ManagedMessage managedMessage = new ManagedMessage(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel);
        ServerDashboard serverDashboard = new ServerDashboard(managedMessage);

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

        ManagedMessage managedMessage = serverDashboard.getManagedMessage();
        managedMessage.updateEntries(Main.getJda());

        try {
            managedMessage.getMessage().delete().complete();
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
        ManagedMessage managedMessage = serverDashboard.getManagedMessage();

        try {
            managedMessage.updateEntries(Main.getJda());
            managedMessage.sendOrEditMessage(new MessageBuilder().setEmbeds(EmbedUtils.createEmbed(serverDashboard, lostArkServersCache).build()));
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Dashboard " + serverDashboard.getName() + "! Probably user removed channel/kicked bot or bot does not have permissions. Removing from internal cache...");
            return false;
        }
    }

    public static void updateAll() {
        synchronized (dashboards) {
            List<ServerDashboard> dashboardsToRemove = new ArrayList<>();

            for (ServerDashboard serverDashboard : dashboards) {
                if (!update(serverDashboard)) {
                    dashboardsToRemove.add(serverDashboard);
                }
            }

            dashboards.removeAll(dashboardsToRemove);
        }
    }

    public static void load() {
        Logger.info("Loading Server Dashboards...");
        dashboards.clear();

        int index = 0;
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(DATA_FILE);
            JsonArray jsonArray = mayuJson.getOrCreate("serverDashboards", new JsonArray()).getAsJsonArray();

            for (JsonElement jsonElement : jsonArray) {
                try {
                    dashboards.add(ServerDashboard.fromJsonObject(jsonElement.getAsJsonObject()));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.warn("Unable to load Server Dashboard with index " + index + "!");
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

        JsonArray jsonArray = new JsonArray();
        try {
            JsonObject jsonObject = new JsonObject();

            jsonArray = new JsonArray();
            for (ServerDashboard serverDashboard : dashboards) {
                jsonArray.add(serverDashboard.toJsonObject());
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
