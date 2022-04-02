package dev.mayuna.lostarkbot.util;

import com.google.gson.*;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.LostArkServersChange;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayuslibrary.utils.ArrayUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Utils {

    public static LostArkRegion getRegionForServer(String serverName) {
        if (Config.getWestNorthAmerica().contains(serverName)) {
            return LostArkRegion.WEST_NORTH_AMERICA;
        } else if (Config.getEastNorthAmerica().contains(serverName)) {
            return LostArkRegion.EAST_NORTH_AMERICA;
        } else if (Config.getCentralEurope().contains(serverName)) {
            return LostArkRegion.CENTRAL_EUROPE;
        } else if (Config.getSouthAmerica().contains(serverName)) {
            return LostArkRegion.SOUTH_AMERICA;
        } else if (Config.getEuropeWest().contains(serverName)) {
            return LostArkRegion.EUROPE_WEST;
        }
        return null;
    }

    public static Map<String, ServerStatus> getServersByRegion(LostArkRegion region, LostArkServers servers) {
        Map<String, ServerStatus> serverStatusMap = new LinkedHashMap<>();
        for (LostArkServer server : servers.getServers()) {
            if (getRegionForServer(server.getName()) == region) {
                serverStatusMap.put(server.getName(), server.getStatus());
            }
        }
        return serverStatusMap;
    }

    public static String getServerLine(String serverName, ServerStatus serverStatus) {
        String serverLine;

        switch (serverStatus) {
            case GOOD -> serverLine = Constants.ONLINE_EMOTE;
            case BUSY -> serverLine = Constants.BUSY_EMOTE;
            case FULL -> serverLine = Constants.FULL_EMOTE;
            case MAINTENANCE -> serverLine = Constants.WARNING_EMOTE;
            default -> serverLine = Constants.NOT_FOUND_EMOTE;
        }

        serverLine += " " + serverName;

        return serverLine;
    }

    public static String getEmoteByStatus(ServerStatus serverStatus) {
        if (serverStatus == null) {
            return Constants.NOT_FOUND_EMOTE;
        }

        return switch (serverStatus) {
            case GOOD -> Constants.ONLINE_EMOTE;
            case BUSY -> Constants.BUSY_EMOTE;
            case FULL -> Constants.FULL_EMOTE;
            case MAINTENANCE -> Constants.WARNING_EMOTE;
        };
    }

    public static ServerStatus getServerStatus(String serverName, LostArkServers servers) {
        for (LostArkServer server : servers.getServers()) {
            if (server.getName().equalsIgnoreCase(serverName)) {
                return server.getStatus();
            }
        }

        return null;
    }

    /**
     * Checks if server exists. If exists, returns correct, possibly same as specified server name, server name.
     * @param serverName Server name
     * @return Null if server does not exist
     */
    public static String doesServerExist(String serverName) {
        for (LostArkServer server : ServerDashboardHelper.getLostArkServersCache().getServers()) {
            if (server.getName().equalsIgnoreCase(serverName)) {
                return server.getName();
            }
        }

        return null;
    }

    public static long toUnixTimestamp(String timestamp) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mm:ss a z", Locale.US));
            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
            return localDateTime.atZone(zoneId).toEpochSecond();
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Cannot parse timestamp! Returning zero.");
            return 0;
        }
    }

    public static void makeEphemeral(SlashCommandEvent event, boolean ephemeral) {
        event.deferReply(ephemeral).complete();
    }

    public static String getOnlinePlayers() {
        try {
            URL url = new URL(Constants.STEAM_API_URL);
            URLConnection request = url.openConnection();
            request.connect();

            JsonObject rootJsonObject = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

            if (rootJsonObject.has("response")) {
                JsonObject responseJsonObject = rootJsonObject.getAsJsonObject("response");
                if (responseJsonObject.has("player_count")) {
                    return String.valueOf(responseJsonObject.get("player_count").getAsInt());
                }
            }

            Logger.error("Invalid response from SteamAPI: " + rootJsonObject);
            return "Error_01";
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Could not get online players in Lost Ark from SteamAPI!");
            return "Error_02";
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public static OptionData getActionArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "action", "Action", true);
        optionData.addChoice("Enable", "enable");
        optionData.addChoice("Disable", "disable");
        return optionData;
    }

    public static OptionData getNewsCategoryArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "category", "News category", true);
        for (NewsCategory newsCategory : NewsCategory.values()) {
            optionData.addChoice(newsCategory.toString(), newsCategory.name());
        }
        return optionData;
    }

    public static OptionData getForumsCategoryArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "category", "Forum category", true);
        for (ForumsCategory forumsCategory : ForumsCategory.values()) {
            optionData.addChoice(forumsCategory.toString(), forumsCategory.name());
        }
        return optionData;
    }

    public static OptionData getRegionArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "region", "Region", true);
        for (LostArkRegion region : LostArkRegion.values()) {
            optionData.addChoice(region.getFormattedName(), region.name());
        }
        return optionData;
    }

    public static OptionData getClearArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "clear", "Determines which notifications should be removed", true);
        optionData.addChoice("News", "news");
        optionData.addChoice("Forums", "forums");
        optionData.addChoice("Status change Server", "status_server");
        optionData.addChoice("Status change Region", "status_region");
        return optionData;
    }

    public static boolean isLast(Object[] array, Object object) {
        return ArrayUtils.getLast(array) == object;
    }

    public static boolean isLast(Collection<?> collection, Object object) {
        return isLast(collection.toArray(), object);
    }

    public static LostArkServer getServerFromList(Collection<LostArkServer> servers, String serverName) {
        for (LostArkServer lostArkServer : servers) {
            if (lostArkServer.getName().equalsIgnoreCase(serverName)) {
                return lostArkServer;
            }
        }

        return null;
    }

    public static List<LostArkServersChange.Difference> getDifferencesByRegion(Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences, LostArkRegion lostArkRegion) {
        List<LostArkServersChange.Difference> differences = new LinkedList<>();

        regionDifferences.forEach((difference, regionMap) -> {
            if (regionMap == lostArkRegion) {
                differences.add(difference);
            }
        });

        return differences;
    }

    public static int countAll(int... numbers) {
        if (numbers == null) {
            return 0;
        }

        int total = 0;
        for (int number : numbers) {
            total += number;
        }

        return total;
    }
}
