package dev.mayuna.lostarkbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mayuna.lostarkbot.api.unofficial.objects.ForumsCategory;
import dev.mayuna.lostarkbot.api.unofficial.objects.NewsCategory;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.LostArkServersChange;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayuslibrary.utils.ArrayUtils;
import dev.mayuna.mayuslibrary.utils.StringUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
     *
     * @param serverName Server name
     *
     * @return Null if server does not exist
     */
    public static String doesServerExist(String serverName) {
        for (LostArkServer server : ServerDashboardManager.getLostArkServersCache().getServers()) {
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

    public static OptionData getEnableDisableArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "action", "Action", true);
        optionData.addChoice("Enable", "enable");
        optionData.addChoice("Disable", "disable");
        return optionData;
    }

    public static OptionData getAddRemoveArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "action", "Action", true);
        optionData.addChoice("Add", "add");
        optionData.addChoice("Remove", "remove");
        return optionData;
    }

    public static OptionData getStatusArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "status", "Status", true);
        for (ServerStatus serverStatus : ServerStatus.values()) {
            if (serverStatus == ServerStatus.GOOD) {
                optionData.addChoice("Online", serverStatus.name());
            } else {
                optionData.addChoice(StringUtils.prettyString(serverStatus.name()), serverStatus.name());
            }
        }
        return optionData;
    }

    public static OptionData getStatusWithOfflineArgument() {
        OptionData optionData = getStatusArgument();
        optionData.addChoice("Offline", "OFFLINE");
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
        optionData.addChoice("Status whitelist", "status_whitelist");
        optionData.addChoice("Ping roles", "ping_roles");
        optionData.addChoice("Twitter Filter", "twitter_filter");
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

    public static String getTimerWithoutMillis(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        String time = "";

        if (minutes != 0) {
            time += minutes + "m ";
        }
        seconds -= minutes * 60;
        if (seconds != 0) {
            time += seconds + "s";
        }

        if (time.equals("")) {
            time = "0s";
        }

        return time;
    }

    public static void waitByConfigValue(UpdateType updateType) {
        try {
            switch (updateType) {
                case SERVER_DASHBOARD -> {
                    long waitTime = Config.getWaitTimeBetweenDashboardUpdates();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case NOTIFICATIONS -> {
                    long waitTime = Config.getWaitTimeBetweenNotificationMessages();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case SERVER_STATUS -> {
                    long waitTime = Config.getWaitTimeBetweenServerStatusMessages();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case TWITTER -> {
                    long waitTime = Config.getWaitTimeBetweenTweets();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
            }
        } catch (Exception exception) {
            Logger.throwing(exception);

            Logger.error("Exception occurred while waiting config value for type " + updateType.name() + "!");
        }
    }

    public static boolean isOnWhitelist(LostArkServersChange.Difference difference, List<String> statusWhitelist) {
        if (statusWhitelist.isEmpty()) {
            return true;
        }

        if (difference.getNewStatus() == null) {
            if (statusWhitelist.contains("OFFLINE")) {
                return true;
            }
        }

        if (difference.getOldStatus() == null) {
            if (statusWhitelist.contains("OFFLINE")) {
                return true;
            }
        }

        for (ServerStatus serverStatus : ServerStatus.values()) {
            if (serverStatus == difference.getNewStatus()) {
                if (statusWhitelist.contains(serverStatus.name())) {
                    return true;
                }
            }

            if (serverStatus == difference.getOldStatus()) {
                if (statusWhitelist.contains(serverStatus.name())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <T> T concatenate(T a, T b) {
        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            throw new IllegalArgumentException();
        }

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        } else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        } else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }
}
