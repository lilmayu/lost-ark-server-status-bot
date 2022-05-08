package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class NotifyRemoveCommand extends SlashCommand {

    public NotifyRemoveCommand() {
        this.name = "remove";
        this.help = "Unmarks current Text Channel from Notification Channel";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.notificationChannelMustExist(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        if (NotificationChannelHelper.deleteNotificationChannel(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully unmarked this Text Channel from Notification Channel.").build()).queue();
            notificationChannel.save();
        } else {
            interactionHook.editOriginalEmbeds(MessageInfo.warningEmbed(
                            "Notification channel could not be unmarked. Please, try again. However, you should not see this message ever.")
                                                       .build()).queue();
        }
    }
}
