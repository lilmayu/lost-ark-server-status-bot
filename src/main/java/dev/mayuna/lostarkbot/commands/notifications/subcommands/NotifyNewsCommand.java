package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.objects.other.StaticNewsTags;
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

public class NotifyNewsCommand extends SlashCommand {

    public NotifyNewsCommand() {
        this.name = "news";
        this.help = "Allows you to enable/disable specific Lost Ark News notifications";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getEnableDisableArgument());
        options.add(Utils.getNewsTagArgument());
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
        OptionMapping newsCategoryOption = AutoMessageUtils.getOptionMapping(event, "tag");

        if (actionOption == null || newsCategoryOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        StaticNewsTags newsTag = StaticNewsTags.get(newsCategoryOption.getAsString());

        if (newsTag == null) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("This News Tag does not exist!").build()).queue();
            return;
        }

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enableNews(newsTag)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for News category **" + newsTag.getDisplayName() + "**!")
                                                                  .build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsTag.getDisplayName() + "** are already enabled!")
                                                                  .build()).queue();
                }
            }
            case "disable" -> {
                if (notificationChannel.disableNews(newsCategoryOption.getAsString())) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for News category **" + newsTag.getDisplayName() + "**!")
                                                                  .build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsTag.getDisplayName() + "** are already disabled!")
                                                                  .build()).queue();
                }
            }
        }
    }
}
