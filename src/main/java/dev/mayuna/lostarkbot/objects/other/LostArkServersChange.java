package dev.mayuna.lostarkbot.objects.other;

import dev.mayuna.lostarkbot.util.LostArkUtils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServers;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkServerStatus;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        return !LostArkUtils.doLostArkServersStatusesEqual(previousServers, currentServers);
    }

    public Difference getDifference(String serverName, LostArkServerStatus oldServerStatus, LostArkServerStatus newServerStatus) {
        Logger.flow("ServerName: " + serverName + "; oldStatus: " + oldServerStatus + "; newStatus: " + newServerStatus);

        if (oldServerStatus == newServerStatus) {
            return null;
        }


        return new Difference(serverName, oldServerStatus, newServerStatus);
    }

    public Difference getDifferenceForServer(String serverName) {
        LostArkServer oldServer = previousServers.getServerByName(serverName).orElse(null);
        LostArkServer newServer = currentServers.getServerByName(serverName).orElse(null);

        LostArkServerStatus oldServerStatus = oldServer == null ? LostArkServerStatus.OFFLINE : oldServer.getStatus();
        LostArkServerStatus newServerStatus = newServer == null ? LostArkServerStatus.OFFLINE : newServer.getStatus();

        return getDifference(serverName, oldServerStatus, newServerStatus);
    }

    public List<Difference> getDifferenceForWholeRegion(LostArkRegion lostArkRegion) {
        List<LostArkServer> oldServers = previousServers.getServersByRegion(lostArkRegion);
        List<LostArkServer> newServers = currentServers.getServersByRegion(lostArkRegion);
        List<Difference> differences = new LinkedList<>();

        for (LostArkServer lostArkServer : oldServers) {
            String serverName = lostArkServer.getName();

            LostArkServerStatus oldStatus = lostArkServer.getStatus();

            LostArkServer lostArkServerNew = LostArkUtils.getServerFromListByName(serverName, newServers);
            LostArkServerStatus newStatus = LostArkServerStatus.OFFLINE;
            if (lostArkServerNew != null) {
                newStatus = lostArkServerNew.getStatus();
            }

            Difference difference = getDifference(serverName, oldStatus, newStatus);

            if (difference == null) {
                continue;
            }

            if (!differences.contains(difference)) {
                differences.add(difference);
            }
        }

        for (LostArkServer lostArkServer : newServers) {
            String serverName = lostArkServer.getName();

            LostArkServerStatus newStatus = lostArkServer.getStatus();

            LostArkServer lostArkServerOld = LostArkUtils.getServerFromListByName(serverName, oldServers);
            LostArkServerStatus oldStatus = LostArkServerStatus.OFFLINE;
            if (lostArkServerOld != null) {
                oldStatus = lostArkServerOld.getStatus();
            }

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

    public static class Difference implements Comparable<Difference> {

        private final @Getter String serverName;

        private final @Getter LostArkServerStatus oldStatus;
        private final @Getter LostArkServerStatus newStatus;

        public Difference(@NonNull String serverName, @Nullable LostArkServerStatus oldStatus, @Nullable LostArkServerStatus newStatus) {
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
