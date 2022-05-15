package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.objects.other.LostArkRegion;
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

public class NotifyStatusRegionCommand extends SlashCommand {

    public NotifyStatusRegionCommand() {
        this.name = "status-region";
        this.help = "Allows you to enable/disable server status change notifications per-region";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getEnableDisableArgument());
        options.add(Utils.getRegionArgument());
        this.options = options;
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

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping regionOption = AutoMessageUtils.getOptionMapping(event, "region");

        if (actionOption == null || regionOption == null) {
            return;
        }

        String correctRegion = LostArkRegion.getCorrect(regionOption.getAsString());

        if (correctRegion == null) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Region with name `" + regionOption.getAsString() + "` does not exist!").build()).queue();
            return;
        }

        LostArkRegion lostArkRegion = LostArkRegion.valueOf(correctRegion);
        String formattedRegionName = lostArkRegion.getFormattedName();

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enable(lostArkRegion)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Region **" + formattedRegionName + "** status change!")
                                                               .build())
                            .queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Region **" + formattedRegionName + "** status change are already enabled!")
                                                               .build()).queue();
                }
            }
            case "disable" -> {
                if (notificationChannel.disable(lostArkRegion)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Region **" + formattedRegionName + "** status change!")
                                                               .build())
                            .queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Region **" + formattedRegionName + "** status change are already disabled!")
                                                               .build()).queue();
                }
            }
        }
    }
}
