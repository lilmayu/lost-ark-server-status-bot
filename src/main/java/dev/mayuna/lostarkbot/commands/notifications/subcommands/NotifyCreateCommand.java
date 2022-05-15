package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class NotifyCreateCommand extends SlashCommand {

    public NotifyCreateCommand() {
        this.name = "create";
        this.help = "Marks current Text Channel as Notification Channel";

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

        if (PermissionUtils.checkPermissionsAndSendIfMissing(textChannel, interactionHook)) {
            return;
        }

        if (!AutoMessageUtils.notificationChannelMustNotExist(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.createNotificationChannel(textChannel);

        if (notificationChannel != null) {
            notificationChannel.save();
            interactionHook.editOriginalEmbeds(MessageInfo.successEmbed(
                            "Successfully marked this Text Channel as Notification Channel.\n\nYou can now enable Notifications in this channel. See `/help` for more information.")
                                                       .build()).queue();
        } else {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed(
                            "There was error while marking this channel as Notification Channel. Please, try again. Check if bot has all necessary permissions. However, you should not see this message ever.")
                                                       .build()).queue();
        }
    }
}
