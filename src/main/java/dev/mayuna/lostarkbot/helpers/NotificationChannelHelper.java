package dev.mayuna.lostarkbot.helpers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.objects.GuildData;
import dev.mayuna.lostarkbot.objects.NotificationChannel;
import dev.mayuna.lostarkbot.objects.Notifications;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.EmbedUtils;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Waiter;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

public class NotificationChannelHelper {

    /**
     * Determines if {@link NotificationChannel} exists in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return true if is, false otherwise
     */
    public static boolean isNotificationChannelInChannel(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).isNotificationChannelInChannel(textChannel);
    }

    /**
     * Tries to create {@link NotificationChannel} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if there already is some {@link NotificationChannel} in specified server
     */
    public static NotificationChannel createNotificationChannel(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).createNotificationChannel(textChannel);
    }

    /**
     * Tries to delete {@link NotificationChannel} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return True if {@link NotificationChannel} was successfully removed
     */
    public static boolean deleteNotificationChannel(@NonNull TextChannel textChannel) {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(textChannel.getGuild());
        return guildData.deleteNotificationChannel(textChannel);
    }

    /**
     * Gets {@link NotificationChannel} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if {@link NotificationChannel} does not exist in specified Text Channel
     */
    public static NotificationChannel getNotificationChannel(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).getNotificationChannel(textChannel);
    }
}
