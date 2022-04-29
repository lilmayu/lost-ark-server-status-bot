package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.topggsdk.TopGGAPI;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class PresenceManager {

    private static @Getter Timer presenceTimer;

    public static void startPresenceTimer() {
        Logger.info("[PRESENCE] Starting presence manager...");

        presenceTimer = new Timer("PresenceWorker");
        presenceTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.debug("Updating presence activity...");
                Main.getMayuShardManager().get().setStatusProvider(shardId -> OnlineStatus.ONLINE);
                Main.getMayuShardManager().get().setActivityProvider(PresenceManager::getActivityProvider);

                MayuShardManager mayuShardManager = Main.getMayuShardManager();

                if (mayuShardManager != null) {
                    if (Config.get().getTopgg().isUpdateTopggBotStats()) {
                        try {
                            TopGGAPI topGGAPI = new TopGGAPI(Config.get().getTopgg().getTopggToken(), String.valueOf(Config.get().getBot().getBotId()));

                            topGGAPI.updateBotStats(mayuShardManager.get().getGuilds().size(), mayuShardManager.get().getShardsTotal()).execute()
                                    .thenAcceptAsync(topGGAPIResponse -> {
                                        Logger.debug("Top.gg bot stats updated successfully.");
                                    });
                        } catch (Exception exception) {
                            Logger.throwing(exception);
                            Logger.warn("Could not update top.gg bot stats!");
                        }
                    }
                }
            }
        }, 0, 3600000);
    }

    public static Activity getActivityProvider(int shardId) {
        MayuShardManager mayuShardManager = Main.getMayuShardManager();

        if (mayuShardManager == null) {
            return Activity.playing("Loading shard manager...");
        } else {
            return Activity.listening(mayuShardManager.get().getGuilds().size() + " guilds | Shard " + shardId);
        }
    }
}
