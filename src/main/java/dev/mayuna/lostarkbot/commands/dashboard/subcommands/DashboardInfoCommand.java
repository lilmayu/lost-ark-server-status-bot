package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DashboardInfoCommand extends SlashCommand {

    public DashboardInfoCommand() {
        this.name = "info";
        this.help = "Shows information about current Server dashboard";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!Utils.makeEphemeral(event, true)) {
            return;
        }
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isBotFullyLoaded(interactionHook)) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);
        long messageId = dashboard.getManagedGuildMessage().getRawGuildID();

        String description = "";
        LanguagePack languagePack = dashboard.getLanguage();
        description += "Current Language: **" + languagePack.getLangName() + "** (`" + languagePack.getLangCode() + "`)\n";

        description += "\n**Hidden regions**\n";
        description += Utils.makeVerticalStringList(dashboard.getHiddenRegions(), "There are no hidden regions.") + "\n";

        description += "\n**Favorite servers**\n";
        description += Utils.makeVerticalStringList(dashboard.getFavoriteServers(), "There are no favorite servers.") + "\n";

        String technicalInformationField = "";
        technicalInformationField += "Message ID: `" + messageId + "`\n";
        technicalInformationField += "Guild ID: `" + textChannel.getGuild().getId() + "`\n";
        technicalInformationField += "ServerDashboard UUID: `" + dashboard.getName() + "`";

        interactionHook.editOriginalEmbeds(MessageInfo.informationEmbed(description, new MessageEmbed.Field("Technical stuff", technicalInformationField, false)).build()).queue();
    }
}