package dev.mayuna.lostarkbot.objects;

import com.google.gson.annotations.Expose;
import dev.mayuna.lostarkbot.managers.DataManager;
import dev.mayuna.mayusjdautils.managed.ManagedGuild;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuildData extends ManagedGuild {

    private @Getter @Setter @Expose List<ServerDashboard> serverDashboards = new ArrayList<>();

    public GuildData(Guild guild) {
        super(UUID.randomUUID().toString(), guild);
    }

    public GuildData(long rawGuildID) {
        super(UUID.randomUUID().toString(), rawGuildID);
    }

    public void addServerDashboard(ServerDashboard serverDashboard) {
        synchronized (this) {
            serverDashboards.add(serverDashboard);
        }
    }

    public void removeServerDashboard(ServerDashboard serverDashboard) {
        synchronized (this) {
            serverDashboards.remove(serverDashboard);
        }
    }

    public void save() {
        DataManager.save(this);
    }
}
