package dev.mayuna.lostarkbot.objects;

import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import lombok.Getter;

import java.util.*;

public class LostArkServersChange {

    private final @Getter LostArkServers previousServers;
    private final @Getter LostArkServers currentServers;

    public LostArkServersChange(LostArkServers previousServers, LostArkServers currentServers) {
        this.previousServers = previousServers;
        this.currentServers = currentServers;
    }

    public boolean hasChangedAnything() {
        if (previousServers == null || currentServers == null) {
            return false;
        }

        return !previousServers.equals(currentServers);
    }

    public Difference getDifferenceForServer(String serverName) {
        LostArkServer oldServer = Utils.getServerFromList(previousServers.getServers(), serverName);
        LostArkServer newServer = Utils.getServerFromList(currentServers.getServers(), serverName);

        if (oldServer != null && newServer != null) {
            if (oldServer.equals(newServer)) {
                return null;
            }

            return new Difference(serverName, oldServer.getStatus(), newServer.getStatus());
        }

        return null;
    }

    public List<Difference> getDifferenceForWholeRegion(LostArkRegion lostArkRegion) {
        Map<String, ServerStatus> oldServers = Utils.getServersByRegion(lostArkRegion, previousServers);
        Map<String, ServerStatus> newServers = Utils.getServersByRegion(lostArkRegion, currentServers);
        List<Difference> differences = new LinkedList<>();

        for (Map.Entry<String, ServerStatus> entry : oldServers.entrySet()) {
            String serverName = entry.getKey();

            ServerStatus oldStatus = entry.getValue();
            ServerStatus newStatus = newServers.get(serverName);

            if (newStatus != null) {
                if (oldStatus != newStatus) {
                    Difference difference = new Difference(serverName, oldStatus, newStatus);

                    if (!differences.contains(difference)) {
                        differences.add(difference);
                    }
                }
            }
        }

        return differences;
    }

    public class Difference {

        private final @Getter String serverName;

        private final @Getter ServerStatus oldStatus;
        private final @Getter ServerStatus newStatus;

        public Difference(String serverName, ServerStatus oldStatus, ServerStatus newStatus) {
            this.serverName = serverName;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Difference))
                return false;
            Difference that = (Difference) o;
            return Objects.equals(serverName, that.serverName) && oldStatus == that.oldStatus && newStatus == that.newStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverName, oldStatus, newStatus);
        }
    }
}
