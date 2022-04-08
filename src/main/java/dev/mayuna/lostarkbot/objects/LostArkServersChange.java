package dev.mayuna.lostarkbot.objects;

import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkscraper.objects.LostArkServer;
import dev.mayuna.lostarkscraper.objects.LostArkServers;
import dev.mayuna.lostarkscraper.objects.ServerStatus;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

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

    public Difference getDifference(String serverName, ServerStatus oldServerStatus, ServerStatus newServerStatus) {
        Logger.flow("ServerName: " + serverName + "; oldStatus: " + oldServerStatus + "; newStatus: " + newServerStatus);

        if (oldServerStatus == newServerStatus) {
            return null;
        }


        return new Difference(serverName, oldServerStatus, newServerStatus);
    }

    public Difference getDifferenceForServer(String serverName) {
        LostArkServer oldServer = Utils.getServerFromList(previousServers.getServers(), serverName);
        LostArkServer newServer = Utils.getServerFromList(currentServers.getServers(), serverName);

        ServerStatus oldServerStatus = oldServer == null ? null : oldServer.getStatus();
        ServerStatus newServerStatus = newServer == null ? null : newServer.getStatus();;

        Difference difference = getDifference(serverName, oldServerStatus, newServerStatus);

        Logger.get().trace("ForServer");
        Logger.get().trace(difference);

        return difference;
    }

    public List<Difference> getDifferenceForWholeRegion(LostArkRegion lostArkRegion) {
        Map<String, ServerStatus> oldServers = Utils.getServersByRegion(lostArkRegion, previousServers);
        Map<String, ServerStatus> newServers = Utils.getServersByRegion(lostArkRegion, currentServers);
        List<Difference> differences = new LinkedList<>();

        for (Map.Entry<String, ServerStatus> entry : oldServers.entrySet()) {
            String serverName = entry.getKey();

            ServerStatus oldStatus = entry.getValue();
            ServerStatus newStatus = newServers.get(serverName);

            Difference difference = getDifference(serverName, oldStatus, newStatus);

            if (difference == null) {
                continue;
            }

            if (!differences.contains(difference)) {
                differences.add(difference);
            }
        }

        for (Map.Entry<String, ServerStatus> entry : newServers.entrySet()) {
            String serverName = entry.getKey();

            ServerStatus oldStatus = oldServers.get(serverName);
            ServerStatus newStatus = entry.getValue();

            Difference difference = getDifference(serverName, oldStatus, newStatus);

            if (difference == null) {
                continue;
            }

            if (!differences.contains(difference)) {
                differences.add(difference);
            }
        }

        Logger.get().trace("ForRegion: " + lostArkRegion);
        Logger.get().trace(differences);

        return differences;
    }

    public class Difference implements Comparable<Difference> {

        private final @Getter String serverName;

        private final @Getter ServerStatus oldStatus;
        private final @Getter ServerStatus newStatus;

        public Difference(@NonNull String serverName, ServerStatus oldStatus, ServerStatus newStatus) {
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

        @Override
        public String toString() {
            return "Difference{" +
                    "serverName='" + serverName + '\'' +
                    ", oldStatus=" + oldStatus +
                    ", newStatus=" + newStatus +
                    '}';
        }

        @Override
        public int compareTo(@NonNull LostArkServersChange.Difference o) {
            return serverName.compareTo(o.serverName);
        }
    }
}
