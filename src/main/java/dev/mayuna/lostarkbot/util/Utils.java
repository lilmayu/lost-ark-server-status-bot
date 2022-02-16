package dev.mayuna.lostarkbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mayuna.lostarkbot.managers.ServerDashboardManager;
import dev.mayuna.lostarkbot.objects.LostArkRegion;
import dev.mayuna.lostarkbot.objects.ServerDashboard;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;

import java.util.LinkedHashMap;
import java.util.Map;

public class Utils {

    public static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ManagedMessage.class, new ServerDashboard()) // TODO: ManagedMessage adapter
                .create();
    }

    public static LostArkRegion getRegionForServer(String serverName) {
        if (Config.getWestNorthAmerica().contains(serverName)) {
            return LostArkRegion.WEST_NORTH_AMERICA;
        } else if (Config.getEastNorthAmerica().contains(serverName)) {
            return LostArkRegion.EAST_NORTH_AMERICA;
        } else if (Config.getCentralEurope().contains(serverName)) {
            return LostArkRegion.CENTRAL_EUROPE;
        } else if (Config.getSouthAmerica().contains(serverName)) {
            return LostArkRegion.SOUTH_AMERICA;
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
            case GOOD -> serverLine = "<:circle_green:943546669558018139> ";
            case BUSY -> serverLine = "<:circle_red:943546670229114911> ";
            case FULL -> serverLine = "<:circle_blue:943546670115848202> ";
            case MAINTENANCE -> serverLine = "<:circle_yellow:943546669688049725> ";
            default -> serverLine = "<:circle_black:943546670166188142> ";
        }

        serverLine += serverName;

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
        for (LostArkServer server : ServerDashboardManager.getLostArkServersCache().getServers()) {
            if (server.getName().equalsIgnoreCase(serverName)) {
                return server.getName();
            }
        }

        return null;
    }
}
