package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.old.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.old.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.other.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.other.MayuTweet;
import dev.mayuna.lostarkbot.objects.features.LanguagePack;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkServerStatus;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class EmbedUtils {

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
                    fields.add(new MessageEmbed.Field(lostArkRegion.getPrettyName(), lines, false));
                    lines = "";
                }

                lines += line + "\n";
            }

            if (!lines.isEmpty()) {
                String fieldName = lostArkRegion.getPrettyName();

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
        int offline = 0;

        for (LostArkServersChange.Difference difference : differences) {
            if (difference == null) {
                continue;
            }

            LostArkServerStatus newStatus = difference.getNewStatus();

            switch (newStatus) {
                case ONLINE -> {
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
                case OFFLINE -> {
                    offline++;
                }
            }
        }

        if (online > Utils.countAll(busy, full, maintenance, offline)) {
            return new Color(171, 195, 70);
        } else if (busy > Utils.countAll(online, full, maintenance, offline)) {
            return new Color(197, 46, 38);
        } else if (full > Utils.countAll(busy, online, maintenance, offline)) {
            return new Color(91, 161, 201);
        } else if (maintenance > Utils.countAll(busy, full, online, offline)) {
            return new Color(250, 227, 74);
        } else if (offline > Utils.countAll(busy, full, maintenance, online)) {
            return new Color(41, 43, 47);
        } else {
            return null;
        }
    }
}
