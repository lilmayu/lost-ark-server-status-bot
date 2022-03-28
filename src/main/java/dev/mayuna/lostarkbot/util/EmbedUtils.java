package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsPostObject;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsObject;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.LanguagePack;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
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

        String onlinePlayers = ServerDashboardHelper.getOnlinePlayersCache();
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
        description += Constants.WARNING_EMOTE + " " + languagePack.getMaintenance();
        embedBuilder.setDescription(description);

        LinkedHashMap<String, String> regionFields = new LinkedHashMap<>();

        for (LostArkRegion region : LostArkRegion.values()) {
            if (serverDashboard.getHiddenRegions().contains(region.name())) {
                continue;
            }

            String fieldName = languagePack.getTranslatedRegionName(region);
            StringBuilder fieldValue = new StringBuilder();

            for (Map.Entry<String, ServerStatus> entry : Utils.getServersByRegion(region, servers).entrySet()) {
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
                    toAppend = Constants.NOT_FOUND_EMOTE + " " + serverName + " (" + languagePack.getNotFound() + ")\n";
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

        fields.addAll(getServerStatusChangeField(regionDifferences));
        fields.addAll(getServerStatusChangeField(serverDifferences));

        EmbedBuilder currentEmbedBuilder = new EmbedBuilder();
        int fieldCount = 0;
        int characterCount = 0;

        for (MessageEmbed.Field field : fields) {
            characterCount += field.getValue().length();

            if (characterCount >= 5000) {
                characterCount = 0;

                embedBuilders.add(currentEmbedBuilder);
                currentEmbedBuilder = new EmbedBuilder();
            } else if (fieldCount > 25) {
                fieldCount = 0;

                embedBuilders.add(currentEmbedBuilder);
                currentEmbedBuilder = new EmbedBuilder();
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
            List<LostArkServersChange.Difference> differences = Utils.getDifferencesByRegion(regionDifferences, lostArkRegion);

            String lines = "";
            boolean moreFieldsForOneRegion = false;

            for (LostArkServersChange.Difference difference : differences) {
                String line = makeDifferenceString(difference);

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
        List<MessageEmbed.Field> fields = new LinkedList<>();

        String lines = "";

        for (LostArkServersChange.Difference difference : differences) {
            String line = makeDifferenceString(difference);

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
        return Utils.getEmoteByStatus(difference.getOldStatus()) + " » " + Utils.getEmoteByStatus(difference.getNewStatus()) + " " + difference.getServerName();
    }
}
