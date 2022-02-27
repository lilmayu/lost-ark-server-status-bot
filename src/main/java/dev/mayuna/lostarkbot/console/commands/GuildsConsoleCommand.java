package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.util.logging.Logger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

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
            int numberOfDashboards = 0;

            // TODO: Toto půjde zoptimalizovat (získávat přímo z GuildData .size listu)
            for (TextChannel textChannel : guild.getTextChannels()) {
                if (ServerDashboardHelper.isServerDashboardInChannel(textChannel)) {
                    numberOfDashboards++;
                }
            }

            if (verbose) {
                Logger.info("[" + counter + "]: " + guild.getIdLong() + " // " + guild.getName() + " // Dashboards: " + numberOfDashboards + " // Owner: " + guild.getOwnerIdLong() + " (" + guild.getOwner() + ")");
            } else {
                Logger.info("[" + counter + "]: " + guild.getIdLong() + " // " + guild.getName() + " // Dashboards: " + numberOfDashboards);
            }

            counter++;
        }
    }
}
