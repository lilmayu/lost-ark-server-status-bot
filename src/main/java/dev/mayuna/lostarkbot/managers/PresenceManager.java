package dev.mayuna.lostarkbot.managers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.logging.Logger;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class PresenceManager {

    public static void init() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("PresenceWorker");

                Logger.debug("Updating presence activity...");
                Main.getJda().getPresence().setActivity(Activity.listening("to " + Main.getJda().getGuilds().size() + " guilds!"));
            }
        }, 0, 3600000);
    }
}
