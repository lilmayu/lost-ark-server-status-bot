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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyTwitterCommand extends SlashCommand {

    public NotifyTwitterCommand() {
        this.name = "twitter";
        this.help = "Allows you to enable/disable Twitter notifications";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(1);
        options.add(Utils.getEnableDisableArgument());
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");

        if (actionOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        switch (actionOption.getAsString()) {
            case "enable" -> {
                notificationChannel.setTwitterEnabled(true);
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed(
                                "Successfully enabled notifications from Twitter!\n\nUse `/notify twitter-filter` command for keyword filtering. You can also change Twitter settings using `/notify twitter-settings` command.")
                                                           .build()).queue();
                notificationChannel.save();
            }
            case "disable" -> {
                notificationChannel.setTwitterEnabled(false);
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications from Twitter!").build()).queue();
                notificationChannel.save();
            }
        }
    }
}
