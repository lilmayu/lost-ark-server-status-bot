package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.util.UpdateType;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.LostArk;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ServerDashboardManager {

    private static final @Getter Timer serverDashboardUpdateWorker = new Timer("ServerDashboardUpdateWorker");

    private static @Getter @Setter LostArkServers lostArkServersCache;
    private static @Getter @Setter LostArkServers previousServerCache;
    private static @Getter String onlinePlayersCache;

    public static void load() {
        serverDashboardUpdateWorker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.info("Updating Lost Ark Server cache...");
                try {
                    updateCache();
                } catch (Exception exception) {
                    Logger.throwing(exception);
                    ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), exception);

                    Logger.error("Could not update Lost Ark Server cache!");
                    return;
                }

                int guildsSize = GuildDataManager.countGuildDataSize();
                Logger.info("Queuing server dashboard updates for " + guildsSize + " guilds...");

                long start = System.currentTimeMillis();
                queueUpdatesForAllGuildData();
                long took = System.currentTimeMillis() - start;

                Logger.info("Queuing server dashboard updates on all shards for " + guildsSize + " done in " + took + "ms");
            }
        }, 0, 300000);
    }

    public static void queueUpdatesForAllGuildData() {
        ShardExecutorManager.submitForEachShard(UpdateType.SERVER_DASHBOARD, shardId -> {
            GuildDataManager.updateAllServerDashboards(shardId);
        });

        ShardExecutorManager.submitForEachShard(UpdateType.SERVER_STATUS, shardId -> {
            GuildDataManager.processServerStatusChange(shardId, previousServerCache, lostArkServersCache);
        });
    }

    public static void updateCache() throws IOException {
        previousServerCache = lostArkServersCache;

        lostArkServersCache = LostArk.fetchServers();
        onlinePlayersCache = Utils.getOnlinePlayers();
    }
}
