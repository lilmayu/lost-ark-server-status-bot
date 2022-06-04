package dev.mayuna.lostarkbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.managers.TwitterManager;
import dev.mayuna.lostarkbot.objects.other.LostArkServersChange;
import dev.mayuna.lostarkbot.objects.other.StatusWhitelistObject;
import dev.mayuna.lostarkbot.objects.other.TwitterUser;
import dev.mayuna.lostarkbot.util.config.Config;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkServerStatus;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayuslibrary.util.ArrayUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String getServerLine(LostArkServer lostArkServer) {
        return getEmoteByStatus(lostArkServer.getStatus()) + " " + lostArkServer.getName();
    }

    public static String getEmoteByStatus(LostArkServerStatus serverStatus) {
        if (serverStatus == null) {
            return Constants.OFFLINE_EMOTE;
        }

        return switch (serverStatus) {
            case ONLINE -> Constants.ONLINE_EMOTE;
            case BUSY -> Constants.BUSY_EMOTE;
            case FULL -> Constants.FULL_EMOTE;
            case MAINTENANCE -> Constants.MAINTENANCE_EMOTE;
            default -> Constants.OFFLINE_EMOTE;
        };
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

    public static boolean makeEphemeral(SlashCommandEvent event, boolean ephemeral) {
        try {
            event.deferReply(ephemeral).complete();
            return true;
        } catch (Exception exception) {
            Logger.get().warn("Exception occurred while acknowledging command interaction!", exception);
            return false;
        }
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

    public static OptionData getShowHideArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "action", "Action", true);
        optionData.addChoice("Show", "show");
        optionData.addChoice("Hide", "hide");
        return optionData;
    }

    public static OptionData getStatusArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "status", "Status", true);
        for (LostArkServerStatus serverStatus : LostArkServerStatus.values()) {
            optionData.addChoice(Utils.prettyString(serverStatus.name()), serverStatus.name());
        }
        return optionData;
    }

    public static OptionData getChangedFromToStatusArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "type", "Type", true);
        optionData.addChoice("Changed from", "from");
        optionData.addChoice("Changed to", "to");
        return optionData;
    }

    public static OptionData getStatusWithOfflineArgument() {
        OptionData optionData = getStatusArgument();
        optionData.addChoice("Offline", "OFFLINE");
        return optionData;
    }

    public static OptionData getNewsTagArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "tag", "News tag", true);

        // TODO: Staticky sem hodit názvy news tagů

        return optionData;
    }

    public static OptionData getForumsCategoryArgument() {
        OptionData optionData = new OptionData(OptionType.NUMBER, "category", "Forum category", true);
        return optionData;
    }

    public static OptionData getRegionArgument() {
        OptionData optionData = new OptionData(OptionType.STRING, "region", "Region", true);
        for (LostArkRegion region : LostArkRegion.values()) {
            optionData.addChoice(region.getPrettyName(), region.name());
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
        optionData.addChoice("Status blacklist", "status_blacklist");
        optionData.addChoice("Status change ping roles", "status_ping_roles");
        optionData.addChoice("Twitter filter", "twitter_filter");
        optionData.addChoice("Twitter ping roles", "twitter_ping_roles");
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

    public static List<LostArkServersChange.Difference> getDifferencesByRegion(Map<LostArkServersChange.Difference, LostArkRegion> regionDifferences,
                                                                               LostArkRegion lostArkRegion) {
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
        Config.WaitTimes waitTimesConfig = Config.get().getWaitTimes();

        try {
            switch (updateType) {
                case SERVER_DASHBOARD -> {
                    long waitTime = waitTimesConfig.getDashboardUpdates();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case NOTIFICATIONS -> {
                    long waitTime = waitTimesConfig.getNotificationMessages();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case SERVER_STATUS -> {
                    long waitTime = waitTimesConfig.getServerStatusMessages();

                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                }
                case TWITTER -> {
                    long waitTime = waitTimesConfig.getTweets();

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

    public static boolean isOnWhitelistAndIsNotOnBlacklist(LostArkServersChange.Difference difference,
                                                           List<StatusWhitelistObject> statusWhitelistObjects,
                                                           List<StatusWhitelistObject> statusBlacklistObjects) {
        if (statusWhitelistObjects.isEmpty()) {
            return true;
        }

        List<String> whitelistStatusesFrom = new LinkedList<>();
        List<String> whitelistStatusesTo = new LinkedList<>();
        List<String> blacklistStatusesFrom = new LinkedList<>();
        List<String> blacklistStatusesTo = new LinkedList<>();

        for (StatusWhitelistObject statusWhitelistObject : statusWhitelistObjects) {
            switch (statusWhitelistObject.getType()) {
                case FROM -> {
                    whitelistStatusesFrom.add(statusWhitelistObject.getStatus());
                }
                case TO -> {
                    whitelistStatusesTo.add(statusWhitelistObject.getStatus());
                }
            }
        }

        for (StatusWhitelistObject statusWhitelistObject : statusBlacklistObjects) {
            switch (statusWhitelistObject.getType()) {
                case FROM -> {
                    blacklistStatusesFrom.add(statusWhitelistObject.getStatus());
                }
                case TO -> {
                    blacklistStatusesTo.add(statusWhitelistObject.getStatus());
                }
            }
        }

        String oldServerStatus = String.valueOf(difference.getOldStatus());
        String newServerStatus = String.valueOf(difference.getNewStatus());

        if (oldServerStatus.equals("null")) {
            oldServerStatus = "OFFLINE";
        }

        if (newServerStatus.equals("null")) {
            newServerStatus = "OFFLINE";
        }

        // == Blacklist == //

        if (blacklistStatusesTo.isEmpty()) {
            for (String status : blacklistStatusesFrom) {
                if (oldServerStatus.equalsIgnoreCase(status)) {
                    return false;
                }
            }
        }

        if (blacklistStatusesFrom.isEmpty()) {
            for (String status : blacklistStatusesTo) {
                if (newServerStatus.equalsIgnoreCase(status)) {
                    return false;
                }
            }
        }

        for (String oldStatus : blacklistStatusesFrom) {
            for (String newStatus : blacklistStatusesTo) {
                if (oldStatus.equalsIgnoreCase(oldServerStatus) && newStatus.equalsIgnoreCase(newServerStatus)) {
                    return false;
                }
            }
        }

        // == Whitelist == //

        if (whitelistStatusesTo.isEmpty()) {
            for (String status : whitelistStatusesFrom) {
                if (oldServerStatus.equalsIgnoreCase(status)) {
                    return true;
                }
            }
        }

        if (whitelistStatusesFrom.isEmpty()) {
            for (String status : whitelistStatusesTo) {
                if (newServerStatus.equalsIgnoreCase(status)) {
                    return true;
                }
            }
        }

        for (String oldStatus : whitelistStatusesFrom) {
            for (String newStatus : whitelistStatusesTo) {
                if (oldStatus.equalsIgnoreCase(oldServerStatus) && newStatus.equalsIgnoreCase(newServerStatus)) {
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

    public static EmbedBuilder getTwitterDefaultEmbed() {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setColor(new Color(29, 161, 242));

        return embedBuilder;
    }

    public static List<String> getUnfollowedUsers(List<String> followedUsers) {
        List<String> unfollowedUsers = new LinkedList<>();

        for (TwitterUser twitterUser : TwitterManager.getFollowingTwitterUsers()) {
            if (!followedUsers.contains(twitterUser.username())) {
                unfollowedUsers.add(twitterUser.username());
            }
        }

        return unfollowedUsers;
    }

    public static String makeHorizontalStringList(List<?> objects, String noValuesMessage) {
        String string = "";

        if (objects == null || objects.isEmpty()) {
            return noValuesMessage;
        }

        for (Object object : objects) {
            string += "`" + object.toString() + "`";

            if (!ArrayUtils.isLast(object, objects.toArray())) {
                string += ", ";
            }
        }

        return string;
    }

    public static String makeVerticalStringList(List<?> objects, String noValuesMessage) {
        String string = "";

        if (objects == null || objects.isEmpty()) {
            return noValuesMessage;
        }

        for (Object object : objects) {
            string += "`" + object.toString() + "`";

            if (!ArrayUtils.isLast(object, objects.toArray())) {
                string += "\n";
            }
        }

        return string;
    }

    public static String prettyString(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public static List<StatusWhitelistObject> getStatusWhitelistObjectsByType(List<StatusWhitelistObject> statusWhitelistObjects,
                                                                              StatusWhitelistObject.Type type) {
        List<StatusWhitelistObject> returnValue = new LinkedList<>();
        for (StatusWhitelistObject statusWhitelistObject : statusWhitelistObjects) {
            if (statusWhitelistObject.getType() == type) {
                returnValue.add(statusWhitelistObject);
            }
        }

        return returnValue;
    }

    public static LostArkServerStatus getServerStatus(String serverName, LostArkServers servers) {
        LostArkServer lostArkServer = servers.getServerByName(serverName).orElse(null);

        if (lostArkServer != null) {
            return lostArkServer.getStatus();
        }

        return LostArkServerStatus.OFFLINE;
    }

    public static String getServerLine(String serverName, LostArkServerStatus serverStatus) {
        return getServerLine(new LostArkServer(serverName, null, serverStatus));
    }

    public enum Direction {
        HORIZONTAL,
        VERTICAL;
    }
}
