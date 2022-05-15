package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DashboardRemoveCommand extends SlashCommand {

    public DashboardRemoveCommand() {
        this.name = "remove";
        this.help = "Removes current Server dashboard";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
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

        if (!AutoMessageUtils.isCorrectChannelType(textChannel, interactionHook)) {
            return;
        }

        if (!AutoMessageUtils.dashboardMustExist(textChannel, interactionHook)) {
            return;
        }

        if (ServerDashboardHelper.deleteServerDashboard(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed Server Dashboard from this channel!").build()).queue();
        } else {
            interactionHook.editOriginalEmbeds(MessageInfo.warningEmbed("Server Dashboard was removed, however message was unable to be deleted.").build()).queue();
        }
    }
}
