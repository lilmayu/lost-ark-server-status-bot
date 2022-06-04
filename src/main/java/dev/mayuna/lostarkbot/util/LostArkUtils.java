package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;

import java.util.ArrayList;
import java.util.List;

public class LostArkUtils {

    public static boolean doLostArkServersEqual(LostArkServers first, LostArkServers second) {
        if (first.get().size() != second.get().size()) {
            return false;
        }

        List<String> serverNames = new ArrayList<>();
        first.get().forEach(lostArkServer -> {
            serverNames.add(lostArkServer.getName());
        });

        second.get().forEach(lostArkServer -> {
            serverNames.remove(lostArkServer.getName());
        });

        return serverNames.size() == 0;
    }

    public static LostArkServer getServerFromListByName(String serverName, List<LostArkServer> servers) {
        for (LostArkServer server : servers) {
            if (server.is(serverName)) {
                return server;
            }
        }

        return null;
    }
}
