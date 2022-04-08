package dev.mayuna.lostarkbot.objects;

import com.google.gson.annotations.Expose;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.managers.GuildDataManager;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.EmbedUtils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.managed.ManagedTextChannel;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;
import java.util.*;

public class NotificationChannel {

    private @Getter @Expose ManagedTextChannel managedTextChannel;

    // News and Forums notifications categories
    private @Getter @Expose List<NewsCategory> newsCategories = new ArrayList<>();
    private @Getter @Expose List<ForumsCategory> forumsCategories = new ArrayList<>();

    // Server / region change
    private @Getter @Expose List<String> servers = new ArrayList<>();
    private @Getter @Expose List<LostArkRegion> regions = new ArrayList<>();

    public NotificationChannel() {
    }

    public NotificationChannel(ManagedTextChannel managedTextChannel) {
        this.managedTextChannel = managedTextChannel;
    }

    public String getName() {
        return managedTextChannel.getName();
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

            managedTextChannel.getTextChannel().sendMessage(messageBuilder.build()).queue();
        }
    }

    public void processServerStatusChange(LostArkServersChange lostArkServersChange) {
        Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences = new LinkedHashMap<>();
        List<LostArkServersChange.Difference> serverDifferences = new LinkedList<>();

        for (LostArkRegion lostArkRegion : getRegions()) {
            lostArkServersChange.getDifferenceForWholeRegion(lostArkRegion).forEach(difference -> {
                regionDifferences.put(difference, lostArkRegion);
            });
        }

        for (String serverName : getServers()) {
            if (serverName == null)
                continue;

            LostArkServersChange.Difference difference = lostArkServersChange.getDifferenceForServer(serverName);

            if (difference == null)
                continue;

            if (!regionDifferences.containsKey(difference)) {
                serverDifferences.add(difference);
            }
        }

        List<EmbedBuilder> embedBuilders = EmbedUtils.createEmbeds(regionDifferences, serverDifferences);

        LanguagePack languagePack = LanguageManager.getDefaultLanguage(); // Cannot use languages :(

        for (EmbedBuilder embedBuilder : embedBuilders) {
            embedBuilder.setTitle("Lost Ark - Server status change");

            String description = "";
            description += Constants.ONLINE_EMOTE + " " + languagePack.getOnline() + " ";
            description += Constants.BUSY_EMOTE + " " + languagePack.getBusy() + " ";
            description += Constants.FULL_EMOTE + " " + languagePack.getFull() + " ";
            description += Constants.WARNING_EMOTE + " " + languagePack.getMaintenance() + " ";
            description += Constants.NOT_FOUND_EMOTE + " Offline";
            embedBuilder.setDescription(description);

            embedBuilder.setFooter("Provided by Mayu's Lost Ark Bot");
            embedBuilder.setTimestamp(Instant.now());

            Logger.flow("[STATUS-CHANGE] Sending status change message into " + managedTextChannel.getTextChannel() + " (" + getName() + ")");

            managedTextChannel.getTextChannel().sendMessage(new MessageBuilder().setEmbeds(embedBuilder.build()).build()).queue();
        }
    }
}
