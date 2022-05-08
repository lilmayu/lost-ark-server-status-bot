package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.other.LostArkRegion;
import dev.mayuna.lostarkbot.objects.other.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.other.MayuTweet;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class EmbedUtils {

    public static EmbedBuilder createEmbed(ServerDashboard serverDashboard, LostArkServers servers) {
        LanguagePack languagePack = LanguageManager.getLanguageByCode(serverDashboard.getLangCode());

        if (languagePack == null) {
            Logger.warn("Missing language: " + serverDashboard.getLangCode());
            languagePack = LanguageManager.getDefaultLanguage();
        }

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle(languagePack.getTitle());

        String onlinePlayers = ServerDashboardManager.getOnlinePlayersCache();
        String lastUpdated = servers.getLastUpdated();

        if (lastUpdated == null || lastUpdated.isEmpty()) {
            lastUpdated = "Error";
        }

        String description = "";
        description += "`" + lastUpdated + "`\n";
        description += languagePack.getCurrentPlayers().replace("{players}", onlinePlayers) + "\n\n";
        description += Constants.ONLINE_EMOTE + " " + languagePack.getOnline() + " ";
        description += Constants.BUSY_EMOTE + " " + languagePack.getBusy() + " ";
        description += Constants.FULL_EMOTE + " " + languagePack.getFull() + " ";
        description += Constants.MAINTENANCE_EMOTE + " " + languagePack.getMaintenance();
        embedBuilder.setDescription(description);

        LinkedHashMap<String, String> regionFields = new LinkedHashMap<>();

        for (LostArkRegion region : LostArkRegion.values()) {
            if (serverDashboard.getHiddenRegions().contains(region.name())) {
                continue;
            }

            String fieldName = languagePack.getTranslatedRegionName(region);
            StringBuilder fieldValue = new StringBuilder();

            for (Map.Entry<String, ServerStatus> entry : new TreeMap<>(Utils.getServersByRegion(region, servers)).entrySet()) {
                String toAppend = Utils.getServerLine(entry.getKey(), entry.getValue()) + "\n";

                if (fieldValue.length() + toAppend.length() < 1024) {
                    fieldValue.append(toAppend);
                } else {
                    Logger.warn("Cannot fit line into Field for region " + region.name() + ": " + toAppend);
                    break;
                }
            }

            regionFields.put(fieldName, fieldValue.toString());
        }

        LinkedHashMap<String, String> sortedRegionFields = new LinkedHashMap<>();
        regionFields.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().length() * -1)) // * -1 for reversed sorting
                .forEachOrdered(x -> sortedRegionFields.put(x.getKey(), x.getValue()));

        int counter = 0;
        for (Map.Entry<String, String> entry : sortedRegionFields.entrySet()) {
            String fieldValue = entry.getValue();

            if (fieldValue.isEmpty()) {
                embedBuilder.addField(entry.getKey(), languagePack.getNoServers(), true);
            } else {
                embedBuilder.addField(entry.getKey(), fieldValue, true);
            }

            if (counter == 1 && regionFields.size() > 2) {
                embedBuilder.addBlankField(false);
                counter = -1;
            }

            counter++;
        }


        if (!serverDashboard.getFavoriteServers().isEmpty()) { // Favorite servers
            String fieldValue = "";

            for (String serverName : serverDashboard.getFavoriteServers()) {
                ServerStatus serverStatus = Utils.getServerStatus(serverName, servers);
                String toAppend;

                if (serverStatus != null) {
                    toAppend = Utils.getServerLine(serverName, serverStatus) + "\n";
                } else {
                    toAppend = Constants.OFFLINE_EMOTE + " " + serverName + "\n";
                }

                if (fieldValue.length() + toAppend.length() < 1024) {
                    fieldValue += toAppend;
                } else {
                    Logger.warn("Cannot fit line into Field for Favorite: " + toAppend);
                    break;
                }
            }

            embedBuilder.addField(languagePack.getFavorite(), fieldValue, false);
        }

        embedBuilder.setFooter(languagePack.getUpdateFooter());

        return embedBuilder;
    }

    public static EmbedBuilder createEmbed(NewsObject newsObject, NewsCategory newsCategory) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setAuthor("Lost Ark News | " + newsCategory.toString(), "https://www.playlostark.com/en-us/news");
        embedBuilder.setTitle(newsObject.getTitle(), newsObject.getUrl());

        if (!newsObject.getDescription().equalsIgnoreCase(newsObject.getExcerpt())) {
            embedBuilder.setDescription(parseDescription(newsObject.getDescription() + "\n\n" + newsObject.getExcerpt(), 3000));
        } else {
            embedBuilder.setDescription(parseDescription(newsObject.getDescription(), 3000));
        }

        embedBuilder.setImage(newsObject.getThumbnailUrl());
        embedBuilder.setFooter("Provided by Mayu's Lost Ark Bot");
        embedBuilder.setTimestamp(Instant.now());

        return embedBuilder;
    }

    public static EmbedBuilder createEmbed(ForumsPostObject forumsPostObject, ForumsCategory forumsCategory) {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setAuthor("Lost Ark Forums | " + forumsCategory.toString(), "https://forums.playlostark.com/");
        embedBuilder.setTitle(forumsPostObject.getTitle(), forumsPostObject.getUrl());
        embedBuilder.setDescription(parseDescription(forumsPostObject.getPost_body(), 3000));
        embedBuilder.setFooter("Author: " + forumsPostObject.getAuthor() + " | Provided by Mayu's Lost Ark Bot");
        embedBuilder.setTimestamp(Instant.now());

        return embedBuilder;
    }

    private static String parseDescription(String fullString, int maxCharacters) {
        if (fullString.length() > maxCharacters) {
            return fullString.substring(0, maxCharacters - 5) + "(...)";
        } else {
            return fullString;
        }
    }

    public static List<EmbedBuilder> createEmbeds(Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences, List<LostArkServersChange.Difference> serverDifferences) {
        List<EmbedBuilder> embedBuilders = new LinkedList<>();
        List<MessageEmbed.Field> fields = new LinkedList<>();

        Color color = getColorBasedOnDifferences(getAllDifferences(regionDifferences, serverDifferences));

        fields.addAll(getServerStatusChangeField(regionDifferences));
        fields.addAll(getServerStatusChangeField(serverDifferences));

        EmbedBuilder currentEmbedBuilder = new EmbedBuilder();
        currentEmbedBuilder.setColor(color);

        int fieldCount = 0;
        int characterCount = 0;

        for (MessageEmbed.Field field : fields) {
            characterCount += field.getValue().length();

            if (characterCount >= 5000) {
                characterCount = 0;

                embedBuilders.add(currentEmbedBuilder);
                currentEmbedBuilder = new EmbedBuilder();
                currentEmbedBuilder.setColor(color);
            } else if (fieldCount > 25) {
                fieldCount = 0;

                embedBuilders.add(currentEmbedBuilder);
                currentEmbedBuilder = new EmbedBuilder();
                currentEmbedBuilder.setColor(color);
            }

            currentEmbedBuilder.addField(field);
            fieldCount++;
        }

        if (!currentEmbedBuilder.getFields().isEmpty()) {
            embedBuilders.add(currentEmbedBuilder);
        }

        return embedBuilders;
    }

    public static List<MessageEmbed.Field> getServerStatusChangeField(Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences) {
        List<MessageEmbed.Field> fields = new LinkedList<>();

        for (LostArkRegion lostArkRegion : LostArkRegion.values()) {
            List<LostArkServersChange.Difference> differences = Utils.getDifferencesByRegion(new TreeMap<>(regionDifferences), lostArkRegion);

            String lines = "";
            boolean moreFieldsForOneRegion = false;

            for (LostArkServersChange.Difference difference : differences) {
                String line = makeDifferenceString(difference);

                if (line == null) {
                    continue;
                }

                if ((lines + line).length() > 1024) {
                    moreFieldsForOneRegion = true;
                    fields.add(new MessageEmbed.Field(lostArkRegion.getFormattedName(), lines, false));
                    lines = "";
                }

                lines += line + "\n";
            }

            if (!lines.isEmpty()) {
                String fieldName = lostArkRegion.getFormattedName();

                if (moreFieldsForOneRegion) {
                    fieldName = " ";
                }

                fields.add(new MessageEmbed.Field(fieldName, lines, false));
            }
        }

        return fields;
    }

    public static List<MessageEmbed.Field> getServerStatusChangeField(List<LostArkServersChange.Difference> differences) {
        try {
            differences.sort(Comparator.comparing(LostArkServersChange.Difference::getServerName));
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.warn("Unknown exception occurred while sorting by alphabet!");

            for (LostArkServersChange.Difference difference : differences) {
                Logger.warn("Name: " + difference);
            }

            Logger.warn("Was something null???");
        }

        List<MessageEmbed.Field> fields = new LinkedList<>();

        String lines = "";

        for (LostArkServersChange.Difference difference : differences) {
            String line = makeDifferenceString(difference);

            if (line == null) {
                continue;
            }

            if ((lines + line).length() > 1024) {
                fields.add(new MessageEmbed.Field("Servers", lines, false));
                lines = "";
            }

            lines += line + "\n";
        }

        if (!lines.isEmpty()) {
            fields.add(new MessageEmbed.Field("Servers", lines, false));
        }

        return fields;
    }

    private static String makeDifferenceString(LostArkServersChange.Difference difference) {
        if (difference == null) {
            return null;
        }

        return Utils.getEmoteByStatus(difference.getOldStatus()) + " » " + Utils.getEmoteByStatus(difference.getNewStatus()) + " " + difference.getServerName();
    }

    private static List<LostArkServersChange.Difference> getAllDifferences(Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences,
            List<LostArkServersChange.Difference> serverDifferences) {
        List<LostArkServersChange.Difference> differences = new LinkedList<>(serverDifferences);
        differences.addAll(regionDifferences.keySet());
        return differences;
    }

    private static Color getColorBasedOnDifferences(List<LostArkServersChange.Difference> differences) {
        int online = 0;
        int busy = 0;
        int full = 0;
        int maintenance = 0;
        int notFound = 0;

        for (LostArkServersChange.Difference difference : differences) {
            if (difference == null) {
                continue;
            }

            ServerStatus newStatus = difference.getNewStatus();

            if (newStatus == null) {
                notFound++;
            } else {
                switch (newStatus) {
                    case GOOD -> {
                        online++;
                    }
                    case BUSY -> {
                        busy++;
                    }
                    case FULL -> {
                        full++;
                    }
                    case MAINTENANCE -> {
                        maintenance++;
                    }
                }
            }
        }

        if (online > Utils.countAll(busy, full, maintenance, notFound)) {
            return new Color(171, 195, 70);
        } else if (busy > Utils.countAll(online, full, maintenance, notFound)) {
            return new Color(197, 46, 38);
        } else if (full > Utils.countAll(busy, online, maintenance, notFound)) {
            return new Color(91, 161, 201);
        } else if (maintenance > Utils.countAll(busy, full, online, notFound)) {
            return new Color(250, 227, 74);
        } else if (notFound > Utils.countAll(busy, full, maintenance, online)) {
            return new Color(41, 43, 47);
        } else {
            return null;
        }
    }

    public static MessageBuilder createTweetMessage(MayuTweet mayuTweet) {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder baseEmbedBuilder = DiscordUtils.getDefaultEmbed();
        List<MessageEmbed> finalEmbeds = new ArrayList<>(4);

        baseEmbedBuilder.setColor(new Color(29, 161, 242));
        baseEmbedBuilder.setFooter("Twitter", Constants.TWITTER_LOGO_URL);

        baseEmbedBuilder.setAuthor(mayuTweet.getUserName() + " (@" + mayuTweet.getUserTag() + ")", mayuTweet.getProfileUrl(), mayuTweet.getProfilePictureUrl());

        String description = "";
        if (mayuTweet.isReply()) {
            description = "*Replied*\n";
        } else if (mayuTweet.isRetweet()) {
            description = "*Retweeted*\n";
        } else if (mayuTweet.isQuoted()) {
            description = "*Quoted*\n";
        } else {
            description = "*Tweeted*\n";
        }

        description += mayuTweet.getFormattedText() + "\n\n";

        if (mayuTweet.isQuoted()) {
            description += "*Quoted tweet*\n";

            MayuTweet quotedMayuTweet = new MayuTweet(mayuTweet.getQuotedStatus());
            description += "[@" + quotedMayuTweet.getUserTag() + "](" + quotedMayuTweet.getProfileUrl() + "): " + quotedMayuTweet.getFormattedText() + "\n\n";
        }

        description += "[See more](" + mayuTweet.getTweetUrl() + ")";
        baseEmbedBuilder.setDescription(description);

        if (mayuTweet.hasMoreMedia()) {
            baseEmbedBuilder.setTitle("\u200E", mayuTweet.getProfileUrl());

            String[] imageUrls = mayuTweet.getMediaUrls();
            baseEmbedBuilder.setImage(imageUrls[0]);

            for (int x = 1; x < imageUrls.length; x++) {
                finalEmbeds.add(new EmbedBuilder().setTitle("\u200E", mayuTweet.getProfileUrl()).setImage(imageUrls[x]).build());
            }
        } else {
            baseEmbedBuilder.setImage(mayuTweet.getMediaUrl());
        }

        finalEmbeds.add(0, baseEmbedBuilder.build());
        messageBuilder.setEmbeds(finalEmbeds);

        return messageBuilder;
    }
}
