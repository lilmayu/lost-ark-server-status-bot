package dev.mayuna.lostarkbot.helpers;

import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.objects.features.GuildData;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

public class ServerDashboardHelper {

    /**
     * Determines if {@link ServerDashboard} exists in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return true if is, false otherwise
     */
    public static boolean isServerDashboardInChannel(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).isServerDashboardInChannel(textChannel);
    }

    /**
     * Tries to create {@link ServerDashboard} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if there already is some {@link ServerDashboard} in specified server or if bot was unable to create/edit message
     */
    public static CompletableFuture<ServerDashboard> createServerDashboard(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).createServerDashboard(textChannel);
    }

    /**
     * Tries to delete {@link ServerDashboard} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return True if {@link ServerDashboard} was successfully removed
     */
    public static CompletableFuture<Boolean> deleteServerDashboard(@NonNull TextChannel textChannel) {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(textChannel.getGuild());
        return guildData.deleteServerDashboard(textChannel);
    }

    /**
     * Gets {@link ServerDashboard} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if {@link ServerDashboard} does not exist in specified Text Channel
     */
    public static ServerDashboard getServerDashboard(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).getServerDashboard(textChannel);
    }
}
