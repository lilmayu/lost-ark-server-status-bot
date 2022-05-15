package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
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

public class NotifyNewsCommand extends SlashCommand {

    public NotifyNewsCommand() {
        this.name = "news";
        this.help = "Allows you to enable/disable specific Lost Ark News notifications";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getEnableDisableArgument());
        options.add(Utils.getNewsCategoryArgument());
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
        OptionMapping newsCategoryOption = AutoMessageUtils.getOptionMapping(event, "category");

        if (actionOption == null || newsCategoryOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        NewsCategory newsCategory = NewsCategory.valueOf(newsCategoryOption.getAsString());

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enable(newsCategory)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for News category **" + newsCategory + "**!").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsCategory + "** are already enabled!").build()).queue();
                }
            }
            case "disable" -> {
                if (notificationChannel.disable(newsCategory)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for News category **" + newsCategory + "**!").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for News category **" + newsCategory + "** are already disabled!").build()).queue();
                }
            }
        }
    }
}
