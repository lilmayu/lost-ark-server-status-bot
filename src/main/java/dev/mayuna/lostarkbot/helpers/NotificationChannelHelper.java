package dev.mayuna.lostarkbot.helpers;

import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.objects.features.GuildData;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

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
