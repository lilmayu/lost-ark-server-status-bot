package dev.mayuna.lostarkbot.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.Waiter;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.managed.ManagedGuild;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import dev.mayuna.mayusjdautils.managed.ManagedTextChannel;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuildData extends ManagedGuild {

    private @Getter @Expose @SerializedName("serverDashboards") List<ServerDashboard> loadedServerDashboards = Collections.synchronizedList(new ArrayList<>());
    private @Getter @Expose @SerializedName("notificationChannels") List<NotificationChannel> loadedNotificationChannels = Collections.synchronizedList(new ArrayList<>());

    public GuildData(Guild guild) {
        super(UUID.randomUUID().toString(), guild);
    }

    public GuildData(long rawGuildID) {
        super(UUID.randomUUID().toString(), rawGuildID);
    }

    public static File getGuildDataFile(long guildID) {
        return new File(Constants.GUILDS_FOLDER + guildID + ".json");
    }

    // Saving

    /**
     * Saves current {@link GuildData}
     */
    public void save() {
        GuildDataManager.saveGuildData(this);
    }

    // === Server Dashboards === //

    /**
     * Adds {@link ServerDashboard} into loaded server dashboards
     *
     * @param serverDashboard Non-null {@link ServerDashboard}
     */
    public void addServerDashboard(@NonNull ServerDashboard serverDashboard) {
        synchronized (loadedServerDashboards) {
            loadedServerDashboards.add(serverDashboard);
        }
    }

    /**
     * Removes {@link ServerDashboard} from loaded server dashboards
     *
     * @param serverDashboard Non-null {@link ServerDashboard}
     */
    public void removeServerDashboard(@NonNull ServerDashboard serverDashboard) {
        synchronized (loadedServerDashboards) {
            loadedServerDashboards.remove(serverDashboard);
        }
    }

    /**
     * Gets {@link ServerDashboard} from loaded dashboards
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if {@link ServerDashboard} does not exist in specified Text Channel
     */
    public ServerDashboard getServerDashboard(@NonNull TextChannel textChannel) {
        synchronized (loadedServerDashboards) {
            Iterator<ServerDashboard> serverDashboardIterator = loadedServerDashboards.listIterator();
            while (serverDashboardIterator.hasNext()) {
                ServerDashboard serverDashboard = serverDashboardIterator.next();

                if (serverDashboard.getManagedGuildMessage().getRawTextChannelID() == textChannel.getIdLong()) {
                    return serverDashboard;
                }
            }
        }

        return null;
    }

    /**
     * Determines if {@link ServerDashboard} exists in specified Text Channel
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return true if is, false otherwise
     */
    public boolean isServerDashboardInChannel(@NonNull TextChannel textChannel) {
        return getServerDashboard(textChannel) != null;
    }

    /**
     * Tries to create {@link ServerDashboard} in specified Text Channel
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if there already is some {@link ServerDashboard} in specified text channel or if bot was unable to create/edit message
     */
    public ServerDashboard createServerDashboard(@NonNull TextChannel textChannel) {
        if (isServerDashboardInChannel(textChannel)) {
            return null;
        }

        ManagedGuildMessage managedGuildMessage = new ManagedGuildMessage(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel, null);
        ServerDashboard serverDashboard = new ServerDashboard(managedGuildMessage);

        Waiter<Boolean> waiter = ServerDashboardHelper.updateServerDashboard(serverDashboard);
        waiter.await();

        if (waiter.getObject()) {
            this.addServerDashboard(serverDashboard);
            this.save();
            return serverDashboard;
        }

        return null;
    }

    /**
     * Tries to delete {@link ServerDashboard} from specified {@link TextChannel}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return True in {@link Waiter#getObject()} if {@link ServerDashboard} was successfully removed
     */
    public Waiter<Boolean> deleteServerDashboard(@NonNull TextChannel textChannel) {
        ServerDashboard serverDashboard = this.getServerDashboard(textChannel);
        Waiter<Boolean> waiter = new Waiter<>(false);

        if (serverDashboard == null) {
            waiter.proceed();
            return waiter;
        }

        this.removeServerDashboard(serverDashboard);
        this.save();

        ManagedGuildMessage managedGuildMessage = serverDashboard.getManagedGuildMessage();
        try {
            managedGuildMessage.updateEntries(Main.getJda(), RestActionMethod.QUEUE, success -> {
                managedGuildMessage.getMessage().delete().queue(successDelete -> {
                    waiter.setObject(true);
                    waiter.proceed();
                }, failureDelete -> {
                    Logger.warn("Failed to remove Server Dashboard's message (" + serverDashboard.getName() + ")! However, it was removed from loaded ones.");

                    waiter.proceed(); // Default false
                });
            }, failure -> {
                Logger.warn("Failed to update entries for Server Dashboard " + serverDashboard.getName() + "! However, it was removed from loaded ones.");

                waiter.proceed(); // Default false
            });
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.warn("Failed to remove Server Dashboard's message (" + serverDashboard.getName() + ")! However, it was removed from loaded ones.");

            waiter.proceed(); // Default false
        }

        return waiter;
    }

    // Updating

    /**
     * Updates all dashboards in current {@link GuildData}
     */
    public void updateDashboards() {
        synchronized (loadedServerDashboards) {
            Iterator<ServerDashboard> serverDashboardIterator = loadedServerDashboards.listIterator();
            while (serverDashboardIterator.hasNext()) {
                ServerDashboardHelper.updateServerDashboard(serverDashboardIterator.next());
            }
        }
    }

    // Loading

    /**
     * Loads dashboard
     */
    public void loadDashboards() {
        if (loadedServerDashboards == null) {
            loadedServerDashboards = new ArrayList<>();
            return;
        }

        synchronized (this) {
            List<ServerDashboard> serverDashboardsToRemove = new ArrayList<>(loadedServerDashboards.size());

            Iterator<ServerDashboard> serverDashboardIterator = loadedServerDashboards.listIterator();
            while (serverDashboardIterator.hasNext()) {
                ServerDashboard serverDashboard = serverDashboardIterator.next();

                CountDownLatch finished = new CountDownLatch(1);
                AtomicBoolean canContinue = new AtomicBoolean(false);
                AtomicBoolean wasSuccessful = new AtomicBoolean(false);

                do {
                    serverDashboard.getManagedGuildMessage().updateEntries(Main.getJda(), RestActionMethod.COMPLETE, success -> {
                        Logger.flow("[GUILD-DATA] Successfully loaded Server Dashboard " + serverDashboard.getName() + " for GuildData " + getRawGuildID() + " (" + getName() + ")");

                        wasSuccessful.set(true);
                        canContinue.set(true);
                        finished.countDown();
                    }, failure -> {
                        if (failure instanceof NonDiscordException nonDiscordException) {
                            Logger.throwing(nonDiscordException);
                            Logger.error("[GUILD-DATA] Non-Discord exception occurred while updating entries in Server Dashboard " + serverDashboard.getName() + " for GuildData " + getRawGuildID() + " (" + getName() + ")! Waiting 1000ms and retrying.");

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException interruptedException) {
                                throw new RuntimeException("[GUILD-DATA] InterruptedException occurred while sleeping!", interruptedException);
                            }
                        } else {
                            Logger.throwing(failure);
                            Logger.warn("[GUILD-DATA] Unable to load Server Dashboard " + serverDashboard.getName() + " for GuildData " + getRawGuildID() + " (" + getName() + ")! Probably bot was kicked or text channel was deleted.");

                            wasSuccessful.set(false);
                            canContinue.set(true);
                            finished.countDown();
                        }
                    });
                } while (!canContinue.get());

                if (!wasSuccessful.get()) {
                    Logger.debug("[GUILD-DATA] Server Dashboard could not be loaded - removing it from loaded Server Dashboards");
                    serverDashboardsToRemove.add(serverDashboard);
                }
            }

            if (serverDashboardsToRemove.size() > 0) {
                Logger.debug("[GUILD-DATA] Removing " + serverDashboardsToRemove.size() + " invalid Server Dashboards...");
                loadedServerDashboards.removeAll(serverDashboardsToRemove);
            }
        }
    }

    // === Server Dashboards === //

    // === Notification Channels === //

    /**
     * Adds {@link NotificationChannel} into loaded notification channels
     *
     * @param notificationChannel Non-null {@link NotificationChannel}
     */
    public void addNotificationChannel(@NonNull NotificationChannel notificationChannel) {
        synchronized (loadedNotificationChannels) {
            loadedNotificationChannels.add(notificationChannel);
        }
    }

    /**
     * Removes {@link NotificationChannel} from loaded notification channels
     *
     * @param notificationChannel Non-null {@link NotificationChannel}
     */
    public void removeNotificationChannel(@NonNull NotificationChannel notificationChannel) {
        synchronized (loadedNotificationChannels) {
            loadedNotificationChannels.remove(notificationChannel);
        }
    }


    /**
     * Gets {@link NotificationChannel} from loaded notification channels
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if {@link NotificationChannel} does not exist in specified Text Channel
     */
    public NotificationChannel getNotificationChannel(@NonNull TextChannel textChannel) {
        synchronized (loadedNotificationChannels) {
            Iterator<NotificationChannel> notificationChannelIterator = loadedNotificationChannels.listIterator();
            while (notificationChannelIterator.hasNext()) {
                NotificationChannel notificationChannel = notificationChannelIterator.next();

                if (notificationChannel.getManagedTextChannel().getRawTextChannelID() == textChannel.getIdLong()) {
                    return notificationChannel;
                }
            }
        }

        return null;
    }

    /**
     * Determines if {@link NotificationChannel} exists in specified Text Channel
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return true if is, false otherwise
     */
    public boolean isNotificationChannelInChannel(@NonNull TextChannel textChannel) {
        return getNotificationChannel(textChannel) != null;
    }

    /**
     * Tries to create {@link NotificationChannel} in specified Text Channel
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Null if there already is some {@link NotificationChannel} in specified text channel
     */
    public NotificationChannel createNotificationChannel(@NonNull TextChannel textChannel) {
        if (isNotificationChannelInChannel(textChannel)) {
            return null;
        }

        ManagedTextChannel managedTextChannel = new ManagedTextChannel(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel);
        NotificationChannel notificationChannel = new NotificationChannel(managedTextChannel);

        addNotificationChannel(notificationChannel);

        return notificationChannel;
    }

    /**
     * Tries to delete {@link NotificationChannel} from specified {@link TextChannel}
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return True if {@link NotificationChannel} was removed
     */
    public boolean deleteNotificationChannel(@NonNull TextChannel textChannel) {
        NotificationChannel notificationChannel = this.getNotificationChannel(textChannel);

        removeNotificationChannel(notificationChannel);

        return !isNotificationChannelInChannel(textChannel);
    }

    /**
     * Loads Notification channels
     */
    public void loadNotificationChannels() {
        if (loadedNotificationChannels == null) {
            loadedNotificationChannels = new ArrayList<>();
            return;
        }

        synchronized (this) {
            List<NotificationChannel> notificationChannelsToRemove = new ArrayList<>(loadedNotificationChannels.size());

            Iterator<NotificationChannel> notificationChannelIterator = loadedNotificationChannels.listIterator();
            while (notificationChannelIterator.hasNext()) {
                NotificationChannel notificationChannel = notificationChannelIterator.next();

                try {
                    notificationChannel.getManagedTextChannel().updateEntries(Main.getJda());

                    Logger.flow("[GUILD-DATA] Successfully loaded Notification Channel " + notificationChannel.getName() + " for GuildData " + getRawGuildID() + " (" + getName() + ")");
                } catch (Exception exception) {
                    Logger.throwing(exception);

                    Logger.error("[GUILD-DATA] Unable to load Notification Channel " + notificationChannel.getName() + " for GuildData " + getRawGuildID() + " (" + getName() + ")! Probably bot was kicked or text channel was deleted.");
                    notificationChannelsToRemove.add(notificationChannel);
                }
            }

            if (!notificationChannelsToRemove.isEmpty()) {
                Logger.debug("[GUILD-DATA] Removing " + notificationChannelsToRemove.size() + " invalid Notification Channels...");
                loadedNotificationChannels.removeAll(notificationChannelsToRemove);
            }
        }
    }

    public void sendUnreadNotificationsByRules(@NonNull Notifications notifications) {
        synchronized (loadedNotificationChannels) {
            Iterator<NotificationChannel> notificationChannelIterator = loadedNotificationChannels.listIterator();
            while (notificationChannelIterator.hasNext()) {
                NotificationChannel notificationChannel = notificationChannelIterator.next();

                notificationChannel.sendUnreadNotificationsByRules(notifications);
            }
        }
    }

    public void processServerStatusChange(LostArkServersChange lostArkServersChange) {
        synchronized (loadedNotificationChannels) {
            Iterator<NotificationChannel> notificationChannelIterator = loadedNotificationChannels.listIterator();
            while (notificationChannelIterator.hasNext()) {
                NotificationChannel notificationChannel = notificationChannelIterator.next();

                notificationChannel.processServerStatusChange(lostArkServersChange);
            }
        }
    }

    // === Notification Channels === //

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (!(object instanceof GuildData))
            return false;

        GuildData guildData = (GuildData) object;

        return Objects.equals(getRawGuildID(), guildData.getRawGuildID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRawGuildID());
    }
}
