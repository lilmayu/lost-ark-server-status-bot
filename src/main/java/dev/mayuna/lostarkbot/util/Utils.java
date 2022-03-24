package dev.mayuna.lostarkbot.util;

import com.google.gson.*;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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
            exception.printStackTrace();
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
            exception.printStackTrace();
            Logger.error("Could not get online players in Lost Ark from SteamAPI!");
            return "Error_02";
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }
}
