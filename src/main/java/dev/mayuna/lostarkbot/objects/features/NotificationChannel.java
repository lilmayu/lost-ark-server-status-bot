package dev.mayuna.lostarkbot.objects.features;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.PersistentServerCacheManager;
import dev.mayuna.lostarkbot.objects.features.lostark.WrappedForumTopic;
import dev.mayuna.lostarkbot.objects.other.*;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkForum;
import dev.mayuna.lostarkfetcher.objects.api.LostArkNews;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkNewsTag;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkServerStatus;
import dev.mayuna.mayusjdautils.managed.ManagedTextChannel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.time.Instant;
import java.util.*;

public class NotificationChannel {

    private @Getter @Expose ManagedTextChannel managedTextChannel;

    // News and Forums notifications categories
    private @Getter @Expose @SerializedName(value = "newsTags", alternate = "newsCategories") List<String> newsTags = new LinkedList<>(); // GENERAL, atd.
    private @Getter @Expose List<Integer> forumCategories = new LinkedList<>(); // Čísla, např. 53
    private @Getter @Expose(serialize = false) @SerializedName("forumsCategories") @Deprecated(forRemoval = true) List<String> forumsCategoriesOld = new LinkedList<>(); // Old

    // Twitter
    private @Getter @Setter @Expose boolean twitterEnabled = false;
    private @Getter @Setter @Expose boolean twitterReplies = true;
    private @Getter @Setter @Expose boolean twitterRetweets = true;
    private @Getter @Setter @Expose boolean twitterQuotes = true;
    private @Getter @Setter @Expose boolean twitterFancyEmbeds = true;
    private @Getter @Expose List<String> twitterKeywords = new LinkedList<>();
    private @Getter @Expose List<String> twitterFollowedAccounts = new LinkedList<>(List.of("playlostark"));

    // Server / region change
    private @Getter @Expose @SerializedName(value = "statusChangeServers", alternate = "servers") List<String> statusChangeServers = new LinkedList<>();
    private @Getter @Expose List<LostArkRegion> statusChangeRegions = new LinkedList<>();
    private @Getter @Expose(serialize = false) @SerializedName("regions") List<String> regionsOld = new LinkedList<>();

    // Status server changes
    private @Getter @Expose List<StatusWhitelistObject> statusWhitelistObjects = new LinkedList<>();
    private @Getter @Expose List<StatusWhitelistObject> statusBlacklistObjects = new LinkedList<>();
    private @Getter @Expose List<String> statusPingRolesIds = new ArrayList<>();
    private @Getter @Expose List<String> twitterPingRolesIds = new ArrayList<>();

    public NotificationChannel() {
    }

    public NotificationChannel(ManagedTextChannel managedTextChannel) {
        this.managedTextChannel = managedTextChannel;
    }

    /**
     * Gets name. Basically returns {@link ManagedTextChannel#getName()}
     *
     * @return {@link ManagedTextChannel#getName()}
     */
    public String getName() {
        return managedTextChannel.getName();
    }

    /**
     * Processes backwards compatibility stuff
     */
    public void processBackwardsCompatibility() {
        if (regionsOld.contains("WEST_NORTH_AMERICA")) {
            statusChangeRegions.add(LostArkRegion.NORTH_AMERICA_WEST);
        }

        if (regionsOld.contains("EAST_NORTH_AMERICA")) {
            statusChangeRegions.add(LostArkRegion.NORTH_AMERICA_EAST);
        }

        if (regionsOld.contains("CENTRAL_EUROPE")) {
            statusChangeRegions.add(LostArkRegion.EUROPE_CENTRAL);
        }

        regionsOld.clear();

        statusWhitelistObjects.forEach(StatusWhitelistObject::processBackwardsCompatibility);
        statusBlacklistObjects.forEach(StatusWhitelistObject::processBackwardsCompatibility);

        for (String category : forumsCategoriesOld) {
            if (category.equalsIgnoreCase("MAINTENANCE") || category.equalsIgnoreCase("DOWNTIME")) {
                if (!forumCategories.contains(53)) {
                    forumCategories.add(53);
                }
            }
        }
        forumsCategoriesOld.clear();
    }

    /**
     * Saves {@link GuildData} which holds this Notification Channel
     */
    public void save() {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(managedTextChannel.getGuild());
        guildData.save();
    }

    /**
     * Enables News from specified News tag
     *
     * @param newsTag {@link LostArkNewsTag}
     *
     * @return True if added, false if there is already this tag
     */
    public boolean enableNews(StaticNewsTags newsTag) {
        if (newsTags.contains(newsTag.getDisplayName())) {
            return false;
        }

        newsTags.add(newsTag.getDisplayName());
        return true;
    }

    /**
     * Disables News from specified News tag
     *
     * @param newsTagName News tag name
     *
     * @return True if the newsTags list was changed
     */
    public boolean disableNews(String newsTagName) {
        return newsTags.remove(newsTagName);
    }

    /**
     * Enables Forum Category by its ID
     *
     * @param forumCategoryId ID (int)
     *
     * @return True if added, false if there is already the category ID
     */
    public boolean enableForumCategory(int forumCategoryId) {
        if (forumCategories.contains(forumCategoryId)) {
            return false;
        }

        forumCategories.add(forumCategoryId);
        return true;
    }

    /**
     * Disables Forum Category by its ID (can be non-existing)
     *
     * @param forumCategoryId ID (int)
     *
     * @return True if the forumCategories list was changed
     */
    public boolean disableForumCategory(Integer forumCategoryId) {
        return forumCategories.remove(forumCategoryId);
    }

    /**
     * Enables status change for specified server
     *
     * @param serverName Server name
     *
     * @return True if added, false if the server does not exist or was already added
     */
    public boolean enableStatusChangeForServer(String serverName) {
        LostArkServer lostArkServer = PersistentServerCacheManager.getServerByName(serverName);

        if (lostArkServer == null) {
            return false;
        }

        serverName = lostArkServer.getName();

        if (statusChangeServers.contains(serverName)) {
            return false;
        }

        statusChangeServers.add(serverName);
        return true;
    }

    /**
     * Disables status change for specified server
     *
     * @param serverName Server name
     *
     * @return True if the statusChangeServers list was changed
     */
    public boolean disableStatusChangeForServer(String serverName) {
        return statusChangeServers.remove(serverName);
    }

    /**
     * Enables status change for specified region
     *
     * @param lostArkRegion {@link LostArkRegion}
     *
     * @return True if added, false if the region does not exist or was already added
     */
    public boolean enableStatusChangeForRegion(LostArkRegion lostArkRegion) {
        if (statusChangeRegions.contains(lostArkRegion)) {
            return false;
        }

        statusChangeRegions.add(lostArkRegion);
        return true;
    }

    /**
     * Disables status change for specified region
     *
     * @param lostArkRegion {@link LostArkRegion}
     *
     * @return True if the statusChangeRegions list was changed
     */
    public boolean disableStatusChangeForRegion(LostArkRegion lostArkRegion) {
        return statusChangeRegions.remove(lostArkRegion);
    }

    /**
     * Adds {@link StatusWhitelistObject} to whitelist
     *
     * @param statusWhitelistObject {@link StatusWhitelistObject}
     *
     * @return True if added, false if it already exists or the status is incorrect
     */
    public boolean addToWhitelist(StatusWhitelistObject statusWhitelistObject) {
        String status = statusWhitelistObject.getStatus();

        try {
            LostArkServerStatus.valueOf(status);
        } catch (Exception ignored) {
            return false;
        }

        if (statusWhitelistObjects.contains(statusWhitelistObject)) {
            return false;
        }

        statusWhitelistObjects.add(statusWhitelistObject);
        return true;
    }

    /**
     * Removes {@link StatusWhitelistObject} from whitelist
     *
     * @param statusWhitelistObject {@link StatusWhitelistObject}
     *
     * @return True if the statusWhitelistObjects list was changed
     */
    public boolean removeFromWhitelist(StatusWhitelistObject statusWhitelistObject) {
        return statusWhitelistObjects.remove(statusWhitelistObject);
    }

    /**
     * Adds {@link StatusWhitelistObject} to blacklist
     *
     * @param statusWhitelistObject {@link StatusWhitelistObject}
     *
     * @return True if added, false if it already exists or the status is incorrect
     */
    public boolean addToBlacklist(StatusWhitelistObject statusWhitelistObject) {
        String status = statusWhitelistObject.getStatus();

        try {
            LostArkServerStatus.valueOf(status);
        } catch (Exception ignored) {
            return false;
        }

        if (statusBlacklistObjects.contains(statusWhitelistObject)) {
            return false;
        }

        statusBlacklistObjects.add(statusWhitelistObject);
        return true;
    }

    /**
     * Removes {@link StatusWhitelistObject} from blacklist
     *
     * @param statusWhitelistObject {@link StatusWhitelistObject}
     *
     * @return True if the statusBlacklistObjects list was changed
     */
    public boolean removeFromBlacklist(StatusWhitelistObject statusWhitelistObject) {
        return statusBlacklistObjects.remove(statusWhitelistObject);
    }

    /**
     * Adds role to ping role ids list
     *
     * @param role {@link Role}
     *
     * @return True if added, false if it already exists
     */
    public boolean addToStatusPingRoleIds(Role role) {
        String roleId = role.getId();

        if (!statusPingRolesIds.contains(roleId)) {
            statusPingRolesIds.add(roleId);
            return true;
        }

        return false;
    }

    /**
     * Removes role ID from the statusPingRolesIds list
     *
     * @param roleId Role ID
     *
     * @return True if the statusPingRolesIds list was changed
     */
    public boolean removeFromStatusPingRoleIds(String roleId) {
        return statusPingRolesIds.remove(roleId);
    }

    /**
     * Removes role from the statusPingRolesIds list
     *
     * @param role {@link Role}
     *
     * @return True if the statusPingRolesIds list was changed
     */
    public boolean removeFromStatusPingRoleIds(Role role) {
        return removeFromStatusPingRoleIds(role.getId());
    }

    /**
     * Adds role to Twitter ping role ids list
     *
     * @param role {@link Role}
     *
     * @return True if added, false if it already exists
     */
    public boolean addToTwitterPingRoleIds(Role role) {
        String roleId = role.getId();

        if (!twitterPingRolesIds.contains(roleId)) {
            twitterPingRolesIds.add(roleId);
            return true;
        }

        return false;
    }

    /**
     * Removes role ID from the twitterPingRolesIds list
     *
     * @param roleId Role ID
     *
     * @return True if the twitterPingRolesIds list was changed
     */
    public boolean removeFromTwitterPingRoleIds(String roleId) {
        return twitterPingRolesIds.remove(roleId);
    }

    /**
     * Removes role from the twitterPingRolesIds list
     *
     * @param role {@link Role}
     *
     * @return True if the twitterPingRolesIds list was changed
     */
    public boolean removeFromTwitterPingRoleIds(Role role) {
        return removeFromTwitterPingRoleIds(role.getId());
    }

    /**
     * Adds Twitter keyword into the twitterKeywords list
     *
     * @param keyword Keyword
     *
     * @return True if added, false if it already exists
     */
    public boolean addToTwitterKeywords(String keyword) {
        if (!twitterKeywords.contains(keyword)) {
            twitterKeywords.add(keyword);
            return true;
        }

        return false;
    }

    /**
     * Removes Twitter keyword from the twitterKeywords list
     *
     * @param keyword Keyword
     *
     * @return True if the twitterKeywords list was changed
     */
    public boolean removeFromTwitterKeywords(String keyword) {
        return twitterKeywords.remove(keyword);
    }

    /**
     * Adds Twitter account into the twitterFollowedAccounts list
     *
     * @param twitterAccount Twitter account
     *
     * @return True if added, false if it already exists
     */
    public boolean addToFollowedTwitterAccounts(String twitterAccount) {
        if (!twitterFollowedAccounts.contains(twitterAccount)) {
            twitterFollowedAccounts.add(twitterAccount);
            return true;
        }

        return false;
    }

    /**
     * Removes Twitter account from the twitterFollowedAccounts list
     *
     * @param twitterAccount Twitter account
     *
     * @return True if the twitterFollowedAccounts list was changed
     */
    public boolean removeFromFollowedTwitterKeywords(String twitterAccount) {
        return twitterFollowedAccounts.remove(twitterAccount);
    }

    /**
     * Sends all unread notifications to specified {@link TextChannel} (if there is a NotificationChannel). Also, calls
     * {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param notifications Non-null {@link Notifications}
     */
    public void sendUnreadNotificationsByRules(@NonNull Notifications notifications) {
        List<MessageBuilder> messageBuilders = new ArrayList<>(5);
        MessageBuilder currentMessageBuilder = new MessageBuilder();
        List<MessageEmbed> embeds = new ArrayList<>();

        for (String newsTags : getNewsTags()) {
            for (LostArkNews lostArkNews : notifications.getNewsByCategory(StaticNewsTags.get(newsTags))) {
                embeds.add(EmbedUtils.createEmbed(lostArkNews).build());

                if (embeds.size() == 1) {
                    currentMessageBuilder.setEmbeds(embeds);
                    embeds.clear();
                    messageBuilders.add(currentMessageBuilder);
                    currentMessageBuilder = new MessageBuilder();
                }
            }
        }

        for (int forumCategoryId : getForumCategories()) {
            for (WrappedForumTopic forumTopic : notifications.getForumTopicsByForumCategoryId(forumCategoryId)) {
                embeds.add(EmbedUtils.createEmbed(forumTopic).build());

                if (embeds.size() == 1) {
                    currentMessageBuilder.setEmbeds(embeds);
                    embeds.clear();
                    messageBuilders.add(currentMessageBuilder);
                    currentMessageBuilder = new MessageBuilder();
                }
            }
        }


        if (!embeds.isEmpty()) {
            currentMessageBuilder.setEmbeds(embeds);
            embeds.clear();
            messageBuilders.add(currentMessageBuilder);
        }

        for (MessageBuilder messageBuilder : messageBuilders) {
            Logger.flow("[NOTIFICATIONS] Sending notification message into " + managedTextChannel.getTextChannel() + " (" + getName() + ")");
            MessageSender.sendMessage(messageBuilder.build(), managedTextChannel.getTextChannel(), UpdateType.NOTIFICATIONS);
        }
    }

    public void processServerStatusChange(LostArkServersChange lostArkServersChange) {
        Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences = new LinkedHashMap<>();
        List<LostArkServersChange.Difference> serverDifferences = new LinkedList<>();

        for (LostArkRegion lostArkRegion : getStatusChangeRegions()) {
            lostArkServersChange.getDifferenceForWholeRegion(lostArkRegion).forEach(difference -> {
                if (Utils.isOnWhitelistAndIsNotOnBlacklist(difference,
                                                           statusWhitelistObjects,
                                                           statusBlacklistObjects
                ) && difference != null) { // TODO: Rework této metody
                    regionDifferences.put(difference, lostArkRegion);
                }
            });
        }

        for (String serverName : getStatusChangeServers()) {
            if (serverName == null) {
                continue;
            }

            LostArkServersChange.Difference difference = lostArkServersChange.getDifferenceForServer(serverName);

            if (difference == null) {
                continue;
            }

            if (!regionDifferences.containsKey(difference)) {
                if (Utils.isOnWhitelistAndIsNotOnBlacklist(difference, statusWhitelistObjects, statusBlacklistObjects)) {
                    serverDifferences.add(difference);
                }
            }
        }

        String content = "";

        List<String> roleIdsToRemove = new LinkedList<>();
        for (String roleId : statusPingRolesIds) {
            Role role = managedTextChannel.getGuild().getRoleById(roleId);

            if (role != null) {
                if ((content + role.getAsMention()).length() > 2000) {
                    continue;
                }

                content += role.getAsMention() + " ";
            } else {
                roleIdsToRemove.add(roleId);
            }
        }
        statusPingRolesIds.removeAll(roleIdsToRemove);

        List<EmbedBuilder> embedBuilders = EmbedUtils.createEmbeds(regionDifferences, serverDifferences);

        LanguagePack languagePack = LanguageManager.getDefaultLanguage(); // Cannot use languages :(

        for (EmbedBuilder embedBuilder : embedBuilders) {
            embedBuilder.setTitle("Lost Ark - Server status change");

            String description = "";
            description += Constants.ONLINE_EMOTE + " " + languagePack.getOnline() + " ";
            description += Constants.BUSY_EMOTE + " " + languagePack.getBusy() + " ";
            description += Constants.FULL_EMOTE + " " + languagePack.getFull() + " ";
            description += Constants.MAINTENANCE_EMOTE + " " + languagePack.getMaintenance() + " ";
            description += Constants.OFFLINE_EMOTE + " Offline";
            embedBuilder.setDescription(description);

            embedBuilder.setFooter("Provided by Mayu's Lost Ark Bot");
            embedBuilder.setTimestamp(Instant.now());

            Logger.flow("[STATUS-CHANGE] Queuing status change message into " + managedTextChannel.getTextChannel() + " (" + getName() + ")");
            MessageSender.sendMessage(new MessageBuilder(content).setEmbeds(embedBuilder.build()).build(),
                                      managedTextChannel.getTextChannel(),
                                      UpdateType.SERVER_STATUS
            );
        }
    }

    public void processMayuTweet(MayuTweet mayuTweet) {
        if (!twitterEnabled) {
            return;
        }

        if (!mayuTweet.doesMatchTwitterUsers(twitterFollowedAccounts)) {
            return;
        }

        if (!mayuTweet.doesMatchKeywords(twitterKeywords)) {
            return;
        }

        if (mayuTweet.isRetweet() && !twitterRetweets) {
            return;
        }

        if (mayuTweet.isReply() && !twitterReplies) {
            return;
        }

        if (mayuTweet.isQuoted() && !twitterQuotes) {
            return;
        }

        Logger.flow("[TWITTER] Queuing Twitter message into " + managedTextChannel.getTextChannel() + " (" + getName() + "); Tweet id: " + mayuTweet.getTweetId());

        String content = "";
        List<String> roleIdsToRemove = new LinkedList<>();
        for (String roleId : twitterPingRolesIds) {
            Role role = managedTextChannel.getGuild().getRoleById(roleId);

            if (role != null) {
                if ((content + role.getAsMention() + "\n\n" + mayuTweet.getTweetUrl()).length() > 2000) {
                    continue;
                }

                content += role.getAsMention() + " ";
            } else {
                roleIdsToRemove.add(roleId);
            }
        }
        twitterPingRolesIds.removeAll(roleIdsToRemove);

        try {
            if (!twitterFancyEmbeds) {
                MessageSender.sendMessage(new MessageBuilder(content + "\n\n" + mayuTweet.getTweetUrl()).build(),
                                          managedTextChannel.getTextChannel(),
                                          UpdateType.TWITTER
                );
            } else {
                MessageBuilder messageBuilder = mayuTweet.getMessageBuilder();
                messageBuilder.setContent(content);

                MessageSender.sendMessage(messageBuilder.build(), managedTextChannel.getTextChannel(), UpdateType.TWITTER);
            }
        } catch (PermissionException | ErrorResponseException e) {
            Logger.get().flow(e);

            Logger.warn("There was an exception while sending tweet to " + managedTextChannel.getTextChannel() + " (" + getName() + ")! (Permission or Response Exception)");
        } catch (Exception exception) {
            Logger.throwing(exception);

            Logger.error("There was an exception while sending tweet to " + managedTextChannel.getTextChannel() + " (" + getName() + ")! (Unknown Exception)");
        }
    }
}
