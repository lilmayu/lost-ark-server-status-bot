package dev.mayuna.lostarkbot.objects.features;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.data.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.other.*;
import dev.mayuna.lostarkbot.util.*;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
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
    private @Getter @Expose List<NewsCategory> newsCategories = new ArrayList<>();
    private @Getter @Expose List<ForumsCategory> forumsCategories = new ArrayList<>();

    // Twitter
    private @Getter @Setter @Expose boolean twitterEnabled = false;
    private @Getter @Setter @Expose boolean twitterReplies = true;
    private @Getter @Setter @Expose boolean twitterRetweets = true;
    private @Getter @Setter @Expose boolean twitterQuotes = true;
    private @Getter @Setter @Expose boolean twitterFancyEmbeds = true;
    private @Getter @Expose List<String> twitterKeywords = new ArrayList<>();
    private @Getter @Expose List<String> twitterFollowedAccounts = new ArrayList<>(List.of("playlostark"));

    // Server / region change
    private @Getter @Expose List<String> servers = new ArrayList<>();
    private @Getter @Expose List<LostArkRegion> regions = new ArrayList<>();

    // Status server changes
    private @Getter @Expose(serialize = false) List<String> statusWhitelist = new ArrayList<>(); // Possible values: GOOD, BUSY, FULL, MAINTENANCE, OFFLINE
    private @Getter @Expose List<StatusWhitelistObject> statusWhitelistObjects = new LinkedList<>();
    private @Getter @Expose List<StatusWhitelistObject> statusBlacklistObjects = new LinkedList<>();
    private @Getter @Expose @SerializedName(value = "statusPingRolesIds", alternate = {"roleIds"}) List<String> statusPingRolesIds = new ArrayList<>();
    private @Getter @Expose List<String> twitterPingRolesIds = new ArrayList<>();

    public NotificationChannel() {
    }

    public NotificationChannel(ManagedTextChannel managedTextChannel) {
        this.managedTextChannel = managedTextChannel;
    }

    public String getName() {
        return managedTextChannel.getName();
    }

    public void processOldStatusWhitelist() {
        if (statusWhitelist.isEmpty()) {
            return;
        }

        Logger.debug("Processing old status whitelist...");

        for (String status : statusWhitelist) {
            addToWhitelist(new StatusWhitelistObject(StatusWhitelistObject.Type.FROM, status));
            addToWhitelist(new StatusWhitelistObject(StatusWhitelistObject.Type.TO, status));
        }
    }

    public void save() {
        GuildData guildData = GuildDataManager.getOrCreateGuildData(managedTextChannel.getGuild());
        guildData.save();
    }

    public boolean enable(NewsCategory newsCategory) {
        if (newsCategories.contains(newsCategory)) {
            return false;
        }

        newsCategories.add(newsCategory);
        return true;
    }

    public boolean disable(NewsCategory newsCategory) {
        return newsCategories.remove(newsCategory);
    }

    public boolean enable(ForumsCategory forumsCategory) {
        if (forumsCategories.contains(forumsCategory)) {
            return false;
        }

        forumsCategories.add(forumsCategory);
        return true;
    }

    public boolean disable(ForumsCategory forumsCategory) {
        return forumsCategories.remove(forumsCategory);
    }

    public boolean enable(String server) {
        if (servers.contains(server)) {
            return false;
        }

        servers.add(server);
        return true;
    }

    public boolean disable(String server) {
        return servers.remove(server);
    }

    public boolean enable(LostArkRegion lostArkRegion) {
        if (regions.contains(lostArkRegion)) {
            return false;
        }

        regions.add(lostArkRegion);
        return true;
    }

    public boolean disable(LostArkRegion lostArkRegion) {
        return regions.remove(lostArkRegion);
    }

    public boolean addToWhitelist(StatusWhitelistObject statusWhitelistObject) {
        String status = statusWhitelistObject.getStatus();

        try {
            ServerStatus.valueOf(status);
        } catch (Exception ignored) {
            if (!status.equalsIgnoreCase("OFFLINE")) {
                return false;
            }
        }

        if (!statusWhitelistObjects.contains(statusWhitelistObject)) {
            statusWhitelistObjects.add(statusWhitelistObject);
            return true;
        }

        return false;
    }

    public boolean removeFromWhitelist(StatusWhitelistObject statusWhitelistObject) {
        return statusWhitelistObjects.remove(statusWhitelistObject);
    }

    public boolean addToBlacklist(StatusWhitelistObject statusWhitelistObject) {
        String status = statusWhitelistObject.getStatus();

        try {
            ServerStatus.valueOf(status);
        } catch (Exception ignored) {
            if (!status.equalsIgnoreCase("OFFLINE")) {
                return false;
            }
        }

        if (!statusBlacklistObjects.contains(statusWhitelistObject)) {
            statusBlacklistObjects.add(statusWhitelistObject);
            return true;
        }

        return false;
    }

    public boolean removeFromBlacklist(StatusWhitelistObject statusWhitelistObject) {
        return statusBlacklistObjects.remove(statusWhitelistObject);
    }

    public boolean addToStatusPingRoleIds(Role role) {
        String roleId = role.getId();

        if (!statusPingRolesIds.contains(roleId)) {
            statusPingRolesIds.add(roleId);
            return true;
        }

        return false;
    }

    public boolean removeFromStatusPingRoleIds(String roleId) {
        return statusPingRolesIds.remove(roleId);
    }

    public boolean removeFromStatusPingRoleIds(Role role) {
        return removeFromStatusPingRoleIds(role.getId());
    }

    public boolean addToTwitterPingRoleIds(Role role) {
        String roleId = role.getId();

        if (!twitterPingRolesIds.contains(roleId)) {
            twitterPingRolesIds.add(roleId);
            return true;
        }

        return false;
    }

    public boolean removeFromTwitterPingRoleIds(String roleId) {
        return twitterPingRolesIds.remove(roleId);
    }

    public boolean removeFromTwitterPingRoleIds(Role role) {
        return removeFromTwitterPingRoleIds(role.getId());
    }

    public boolean addToTwitterKeywords(String keyword) {
        if (!twitterKeywords.contains(keyword)) {
            twitterKeywords.add(keyword);
            return true;
        }

        return false;
    }

    public boolean removeFromTwitterKeywords(String keyword) {
        return twitterKeywords.remove(keyword);
    }

    public boolean addToFollowedTwitterAccounts(String twitterAccount) {
        if (!twitterFollowedAccounts.contains(twitterAccount)) {
            twitterFollowedAccounts.add(twitterAccount);
            return true;
        }

        return false;
    }

    public boolean removeFromFollowedTwitterKeywords(String twitterAccount) {
        return twitterFollowedAccounts.remove(twitterAccount);
    }

    /**
     * Sends all unread notifications to specified {@link TextChannel} (if there is a NotificationChannel). Also, calls {@link GuildDataManager#getOrCreateGuildData(Guild)}
     *
     * @param notifications Non-null {@link Notifications}
     */
    public void sendUnreadNotificationsByRules(@NonNull Notifications notifications) {
        List<MessageBuilder> messageBuilders = new ArrayList<>(5);
        MessageBuilder currentMessageBuilder = new MessageBuilder();
        List<MessageEmbed> embeds = new ArrayList<>();

        for (NewsCategory newsCategory : getNewsCategories()) {
            for (NewsObject newsObject : notifications.getNewsByCategory(newsCategory)) {
                embeds.add(EmbedUtils.createEmbed(newsObject, newsCategory).build());

                if (embeds.size() == 1) {
                    currentMessageBuilder.setEmbeds(embeds);
                    embeds.clear();
                    messageBuilders.add(currentMessageBuilder);
                    currentMessageBuilder = new MessageBuilder();
                }
            }
        }

        for (ForumsCategory forumsCategory : getForumsCategories()) {
            for (ForumsPostObject forumsPostObject : notifications.getForumsByCategory(forumsCategory)) {
                embeds.add(EmbedUtils.createEmbed(forumsPostObject, forumsCategory).build());

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

        for (LostArkRegion lostArkRegion : getRegions()) {
            lostArkServersChange.getDifferenceForWholeRegion(lostArkRegion).forEach(difference -> {
                if (Utils.isOnWhitelistAndIsNotOnBlacklist(difference, statusWhitelistObjects, statusBlacklistObjects) && difference != null) {
                    regionDifferences.put(difference, lostArkRegion);
                }
            });
        }

        for (String serverName : getServers()) {
            if (serverName == null)
                continue;

            LostArkServersChange.Difference difference = lostArkServersChange.getDifferenceForServer(serverName);

            if (difference == null)
                continue;

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
            MessageSender.sendMessage(new MessageBuilder(content).setEmbeds(embedBuilder.build()).build(), managedTextChannel.getTextChannel(), UpdateType.SERVER_STATUS);
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
                MessageSender.sendMessage(new MessageBuilder(content + "\n\n" + mayuTweet.getTweetUrl()).build(), managedTextChannel.getTextChannel(), UpdateType.TWITTER);
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
