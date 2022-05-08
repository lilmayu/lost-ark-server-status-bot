package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyForumsCommand extends SlashCommand {

    public NotifyForumsCommand() {
        this.name = "forums";
        this.help = "Allows you to enable/disable specific Lost Ark Forum post notifications";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getEnableDisableArgument());
        options.add(Utils.getForumsCategoryArgument());
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping forumsCategoriesOption = AutoMessageUtils.getOptionMapping(event, "category");

        if (actionOption == null || forumsCategoriesOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        ForumsCategory forumsCategory = ForumsCategory.valueOf(forumsCategoriesOption.getAsString());

        switch (actionOption.getAsString()) {
            case "enable" -> {
                if (notificationChannel.enable(forumsCategory)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully enabled notifications for Forums category **" + forumsCategory + "**!").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Forums category **" + forumsCategory + "** are already enabled!").build()).queue();
                }
            }
            case "disable" -> {
                if (notificationChannel.disable(forumsCategory)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully disabled notifications for Forums category **" + forumsCategory + "**!").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Notifications for Forums category **" + forumsCategory + "** are already disabled!").build()).queue();
                }
            }
        }
    }
}
