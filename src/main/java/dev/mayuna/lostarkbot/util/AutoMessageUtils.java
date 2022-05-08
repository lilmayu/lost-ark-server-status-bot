package dev.mayuna.lostarkbot.util;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AutoMessageUtils {

    /**
     * Returns true if everything is alright
     */
    public static boolean isEverythingAlrightDashboard(TextChannel textChannel, InteractionHook interactionHook) {
        if (!isCorrectChannelType(textChannel, interactionHook)) {
            return false;
        }

        if (PermissionUtils.checkPermissionsAndSendIfMissing(textChannel, interactionHook)) {
            return false;
        }

        if (!dashboardMustExist(textChannel, interactionHook)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if everything is alright
     */
    public static boolean isEverythingAlrightNotificationChannel(TextChannel textChannel, InteractionHook interactionHook) {
        if (!isCorrectChannelType(textChannel, interactionHook)) {
            return false;
        }

        if (PermissionUtils.checkPermissionsAndSendIfMissing(textChannel, interactionHook)) {
            return false;
        }

        if (!notificationChannelMustExist(textChannel, interactionHook)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if it is correct channel type
     */
    public static boolean isCorrectChannelType(Channel channel, InteractionHook interactionHook) {
        ChannelType channelType = channel.getType();

        if (channelType != ChannelType.TEXT) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("This command can be used only in normal Text Channel!").build()).queue();
            return false;
        }

        return true;
    }

    /**
     * Returns true if dashboard does exist
     */
    public static boolean dashboardMustExist(TextChannel textChannel, InteractionHook interactionHook) {
        if (!ServerDashboardHelper.isServerDashboardInChannel(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("There is no Server Dashboard in this channel!\n\nYou can create one using `/dashboard create` command.").build()).queue();
            return false;
        }

        return true;
    }

    /**
     * Returns true if dashboard does not exist
     */
    public static boolean dashboardMustNotExist(TextChannel textChannel, InteractionHook interactionHook) {
        if (ServerDashboardHelper.isServerDashboardInChannel(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("There is already Server Dashboard in this channel!").build()).queue();
            return false;
        }

        return true;
    }

    /**
     * Returns true if dashboard does exist
     */
    public static boolean notificationChannelMustExist(TextChannel textChannel, InteractionHook interactionHook) {
        if (!NotificationChannelHelper.isNotificationChannelInChannel(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel isn't marked as Notification Channel!\n\nYou can mark this channel as Notification Channel using `/notify create` command.").build()).queue();
            return false;
        }

        return true;
    }

    /**
     * Returns true if dashboard does not exist
     */
    public static boolean notificationChannelMustNotExist(TextChannel textChannel, InteractionHook interactionHook) {
        if (NotificationChannelHelper.isNotificationChannelInChannel(textChannel)) {
            interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("This channel is already marked as Notification Channel!").build()).queue();
            return false;
        }

        return true;
    }

    /**
     * Gets {@link OptionMapping}, if not found, sends an error message
     * @param event SlashCommandEvent
     * @param name Name of option
     * @return {@link OptionMapping} if the specified option can be found, null otherwise
     */
    public static OptionMapping getOptionMapping(SlashCommandEvent event, String name) {
        OptionMapping optionMapping = event.getOption(name);

        if (optionMapping == null) {
            event.getHook().editOriginalEmbeds(MessageInfo.errorEmbed("Missing `" + name + "` argument.").build()).queue();
            return null;
        }

        return optionMapping;
    }

    public static void sendInvalidArgumentMessage(InteractionHook interactionHook, String name) {
        interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Invalid `" + name + "` argument.").build()).queue();
    }
}
