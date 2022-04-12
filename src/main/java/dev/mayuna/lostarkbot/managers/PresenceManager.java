package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class PresenceManager {

    private static @Getter Timer presenceTimer;

    public static void startPresenceTimer() {
        presenceTimer = new Timer("PresenceWorker");
        presenceTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Logger.debug("Updating presence activity...");
                Main.getMayuShardManager().get().setStatusProvider(shardId -> OnlineStatus.ONLINE);
                Main.getMayuShardManager().get().setActivityProvider(PresenceManager::getActivityProvider);
            }
        }, 0, 3600000);
    }

    public static Activity getActivityProvider(int shardId) {
        // TODO: Nějak udělat, ať u shardu, kde se to ještě nenačetlo je Loading shard...

        MayuShardManager mayuShardManager = Main.getMayuShardManager();

        if (mayuShardManager == null) {
            return Activity.playing("Loading shard manager...");
        } else {
            return Activity.listening(mayuShardManager.get().getGuilds().size() + " guilds | Shard " + shardId);
        }
    }
}
