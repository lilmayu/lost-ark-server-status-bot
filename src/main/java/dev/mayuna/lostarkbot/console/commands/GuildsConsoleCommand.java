package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayuslibrary.arguments.ArgumentParser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class GuildsConsoleCommand extends AbstractConsoleCommand {

    public GuildsConsoleCommand() {
        this.name = "guilds";
    }

    @Override
    public void execute(String arguments) {
        List<Guild> guilds = Main.getJda().getGuilds();

        int counter = -1;
        for (Guild guild : guilds) {
            counter++;

            GuildData guildData = GuildDataManager.getGuildData(guild);

            if (guildData == null) {
                Logger.warn("[" + counter +  "]: " + guild.getIdLong() + " // Does not have GuildData");
                continue;
            }

            printGuild(guildData, counter, arguments.contains("verbose"));
        }

        Logger.info("=== Connected guilds - " + guilds.size() + " ===");
    }

    private void printGuild(GuildData guildData, int counter, boolean verbose) {
        Logger.info("[" + counter + "]: " + guildData.getRawGuildID() + " // " + guildData.getGuild().getName());
        Logger.info(" - D: " + guildData.getLoadedServerDashboards().size() + " | N: " + guildData.getLoadedNotificationChannels().size());

        if (verbose) {
            Logger.info(" - U: " + guildData.getGuild().getMemberCount() + " | C: " + guildData.getGuild().getOwnerIdLong());
        }
    }
}
