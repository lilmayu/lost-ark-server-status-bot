package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DashboardCreateCommand extends SlashCommand {

    public DashboardCreateCommand() {
        this.name = "create";
        this.help = "Creates a Server dashboard";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isCorrectChannelType(textChannel, interactionHook)) {
            return;
        }

        if (PermissionUtils.checkPermissionsAndSendIfMissing(textChannel, interactionHook)) {
            return;
        }

        if (!AutoMessageUtils.dashboardMustNotExist(textChannel, interactionHook)) {
            return;
        }

        ServerDashboard dashboard = ServerDashboardHelper.createServerDashboard(textChannel);

        if (dashboard != null) {
            interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully created Server Dashboard.").build())
                    .queue();
        } else {
            if (!textChannel.canTalk()) {
                interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed(
                                "Bot cannot send messages in this channel. Please, check bot's permissions in your server! (Write Messages and View Channel permissions)").build())
                        .queue();
            } else {
                interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed(
                                "There was error while creating Server Dashboard. Please, try again. Check if bot has View Channel permission!")
                                                           .build()).queue();
            }
        }
    }
}
