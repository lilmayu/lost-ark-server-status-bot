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
        presenceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Logger.debug("Updating presence activity...");
                Main.getJda().getPresence().setStatus(OnlineStatus.ONLINE);
                Main.getJda().getPresence().setActivity(Activity.listening(Main.getJda().getGuilds().size() + " guilds!"));
            }
        }, 0, 3600000);
    }
}
