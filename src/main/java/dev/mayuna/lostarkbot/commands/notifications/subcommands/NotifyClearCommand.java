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

public class NotifyClearCommand extends SlashCommand {

    public NotifyClearCommand() {
        this.name = "clear";
        this.help = "Allows you to clear specific notifications or setting in current Notification Channel";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(1);
        options.add(Utils.getClearArgument());
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

        OptionMapping clearOption = AutoMessageUtils.getOptionMapping(event, "clear");

        if (clearOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);

        switch (clearOption.getAsString()) {
            case "news" -> {
                notificationChannel.getNewsCategories().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared News notifications!").build()).queue();
            }
            case "forums" -> {
                notificationChannel.getForumsCategories().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Forums notifications!").build()).queue();
            }
            case "status_server" -> {
                notificationChannel.getServers().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Server change status notifications!").build()).queue();
            }
            case "status_region" -> {
                notificationChannel.getRegions().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Region change status notifications!").build()).queue();
            }
            case "status_whitelist" -> {
                notificationChannel.getStatusWhitelistObjects().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Status whitelist!").build()).queue();
            }
            case "status_blacklist" -> {
                notificationChannel.getStatusBlacklistObjects().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Status blacklist!").build()).queue();
            }
            case "status_ping_roles" -> {
                notificationChannel.getStatusPingRolesIds().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Status change ping roles!").build()).queue();
            }
            case "twitter_filter" -> {
                notificationChannel.getTwitterKeywords().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Twitter filter keywords!").build()).queue();
            }
            case "twitter_ping_roles" -> {
                notificationChannel.getTwitterPingRolesIds().clear();
                interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully cleared Twitter ping roles!").build()).queue();
            }
        }

        notificationChannel.save();
    }
}
