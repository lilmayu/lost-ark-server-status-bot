package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.util.logging.Logger;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class GuildsConsoleCommand extends AbstractConsoleCommand {

    public GuildsConsoleCommand() {
        this.name = "guilds";
    }

    @Override
    public void execute(String arguments) {
        boolean verbose = arguments.contains("verbose");

        List<Guild> guilds = Main.getJda().getGuilds();

        Logger.info("=== Connected guilds - " + guilds.size() + " ===");

        int counter = 0;
        for (Guild guild : guilds) {
            if (verbose) {
                Logger.info("[" + counter + "]: " + guild.getIdLong() + " // " + guild.getName() + " // Owner: " + guild.getOwnerIdLong() + " (" + guild.getOwner() + ")");
            } else {
                Logger.info("[" + counter + "]: " + guild.getIdLong() + " // " + guild.getName());
            }

            counter++;
        }
    }
}
