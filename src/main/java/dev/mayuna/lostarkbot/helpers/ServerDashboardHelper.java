package dev.mayuna.lostarkbot.helpers;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.core.GuildData;
import dev.mayuna.lostarkbot.objects.core.ServerDashboard;
import dev.mayuna.lostarkbot.util.EmbedUtils;
import dev.mayuna.lostarkbot.util.PermissionUtils;
import dev.mayuna.lostarkbot.util.Waiter;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import lombok.NonNull;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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
    public static ServerDashboard createServerDashboard(@NonNull TextChannel textChannel) {
        return GuildDataManager.getOrCreateGuildData(textChannel.getGuild()).createServerDashboard(textChannel);
    }

    /**
     * Tries to delete {@link ServerDashboard} in specified {@link TextChannel}. Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return True if {@link ServerDashboard} was successfully removed
     */
    public static boolean deleteServerDashboard(@NonNull TextChannel textChannel) {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(textChannel.getGuild());
        Waiter<Boolean> waiter = guildData.deleteServerDashboard(textChannel);
        waiter.await();
        return waiter.getObject();
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

    // Updating

    /**
     * Updates specified {@link ServerDashboard}
     *
     * @param serverDashboard Non-null {@link ServerDashboard}
     *
     * @return {@link Waiter<Boolean>}. In {@link Waiter#getObject()} is boolean, which determines, if update was successful (true) or failure (false)
     */
    public static Waiter<Boolean> updateServerDashboard(@NonNull ServerDashboard serverDashboard) {
        Waiter<Boolean> waiter = new Waiter<>(false);

        ManagedGuildMessage managedGuildMessage = serverDashboard.getManagedGuildMessage();

        try {
            if (PermissionUtils.isMissingPermissions(managedGuildMessage.getGuild().getSelfMember(), managedGuildMessage.getTextChannel())) {
                Logger.flow("Bot has missing permissions in channel " + managedGuildMessage.getTextChannel() + " (" + managedGuildMessage.getGuild() + ")! Dashboard will be not updated.");

                waiter.setObject(false);
                waiter.proceed();
                return waiter;
            }
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.warn("Failed to check for permissions for Server Dashboard " + serverDashboard.getName() + "! Probably user kicked the bot.");

            waiter.setObject(false);
            waiter.proceed();
            return waiter;
        }

        managedGuildMessage.updateEntries(Main.getMayuShardManager().get(), RestActionMethod.QUEUE, entriesSuccess -> {
            Message message = new MessageBuilder().setEmbeds(EmbedUtils.createEmbed(serverDashboard, ServerDashboardManager.getLostArkServersCache()).build()).build();

            try {
                managedGuildMessage.sendOrEditMessage(message, RestActionMethod.QUEUE, success -> {
                    Logger.flow("Successfully updated dashboard " + serverDashboard.getName() + " with result " + success);

                    waiter.setObject(true);
                    waiter.proceed();
                }, exception -> {
                    Logger.throwing(exception);

                    if (exception instanceof NonDiscordException) {
                        Logger.error("Non Discord Exception occurred while updating dashboard " + serverDashboard.getName() + " in guild " + serverDashboard.getManagedGuildMessage().getRawGuildID() + "!");
                    } else {
                        Logger.warn("Dashboard " + serverDashboard.getName() + " in guild " + serverDashboard.getManagedGuildMessage().getRawGuildID() + " resulted in exception while updating! Probably bot was kicked, text channel deleted or bot does not have permission.");
                    }

                    waiter.setObject(false);
                    waiter.proceed();
                });
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.warn("Dashboard " + serverDashboard.getName() + " in guild " + serverDashboard.getManagedGuildMessage().getRawGuildID() + " resulted in exception while updating! Probably bot was kicked, text channel deleted or bot does not have permission.");
            }
        }, failure -> {
            Logger.throwing(failure);
            Logger.warn("Failed to update entries for Server Dashboard " + serverDashboard.getName() + "! Probably user removed channel/kicked bot or bot does not have permissions.");

            waiter.setObject(false);
            waiter.proceed();
        });

        return waiter;
    }

    // Handling
}
