package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
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

        LostArkRegion lostArkRegion = LostArkRegion.get(regionOption.getAsString());

        if (lostArkRegion == null) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Region with name `" + regionOption.getAsString() + "` does not exist!").build()).queue();
            return;
        }

        String formattedRegionName = lostArkRegion.getPrettyName();

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enableStatusChangeForRegion(lostArkRegion)) {
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
                if (notificationChannel.disableStatusChangeForRegion(lostArkRegion)) {
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
