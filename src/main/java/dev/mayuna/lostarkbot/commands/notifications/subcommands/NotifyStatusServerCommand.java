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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyStatusServerCommand extends SlashCommand {

    public NotifyStatusServerCommand() {
        this.name = "status-server";
        this.help = "Allows you to enable/disable server status change notifications per-server";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getEnableDisableArgument());
        options.add(new OptionData(OptionType.STRING, "server", "Server name", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping serverOption = AutoMessageUtils.getOptionMapping(event, "server");

        if (actionOption == null || serverOption == null) {
            return;
        }

        String correctServerName = Utils.getCorrectServerName(serverOption.getAsString());

        if (correctServerName == null) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` does not exist!").build()).queue();
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enable(correctServerName)) {
                    notificationChannel.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Server **" + correctServerName + "** status change!")
                                                               .build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Server **" + correctServerName + "** status change are already enabled!")
                                                               .build()).queue();
                }
            }
            case "disable" -> {
                if (notificationChannel.disable(correctServerName)) {
                    notificationChannel.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Server **" + correctServerName + "** status change!")
                                                               .build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Server **" + correctServerName + "** status change are already disabled!")
                                                               .build()).queue();
                }
            }
        }
    }
}
