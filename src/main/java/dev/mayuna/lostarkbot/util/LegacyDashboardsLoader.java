package dev.mayuna.lostarkbot.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class LegacyDashboardsLoader {

    public static final String DATA_FILE = "./server_dashboards.json";

    public static void load() {
        Logger.info("Loading legacy Server Dashboards...");
        List<ServerDashboard> dashboards = new LinkedList<>();

        int index = 0;
        try {
            if (!new File(DATA_FILE).exists()) {
                Logger.debug("No legacy dashboards found.");
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
                                Logger.throwing(nonDiscordException);
                                Logger.error("Non-Discord exception occurred while updating entries in Server Dashboard " + name + "! Waiting 1000ms and retrying.");

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException interruptedException) {
                                    throw new RuntimeException("InterruptedException occurred while sleeping!", interruptedException);
                                }
                            } else {
                                Logger.throwing(failure);
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
                    Logger.throwing(exception);
                    Logger.warn("Unable to load JSON Server Dashboard with index " + index + "!");
                }

                index++;
            }

            if (dashboards.size() == 0) {
                Logger.debug("No legacy dashboards found.");

                Logger.info("Deleting legacy dashboards file...");
                new File(DATA_FILE).delete();
                return;
            }

            for (ServerDashboard serverDashboard : dashboards) {
                GuildData guildData = GuildDataManager.getOrCreateGuildData(serverDashboard.getManagedGuildMessage().getGuild());
                guildData.addServerDashboard(serverDashboard);
            }

            Logger.info("Deleting legacy dashboards file...");
            new File(DATA_FILE).delete();

            GuildDataManager.saveAll();

            Logger.success("Successfully loaded " + dashboards.size() + " legacy Server Dashboards into guilds!");
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Failed to load Server Dashboards!");
        }
    }
}
