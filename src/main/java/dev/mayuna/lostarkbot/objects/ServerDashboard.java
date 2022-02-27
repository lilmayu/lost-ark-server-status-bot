package dev.mayuna.lostarkbot.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ServerDashboard {

    private @Getter @Expose @SerializedName("managedMessage") ManagedGuildMessage managedGuildMessage;

    private @Getter @Setter @Expose String langCode = "en";
    private @Getter @Expose List<String> hiddenRegions = new ArrayList<>();
    private @Getter @Expose List<String> favoriteServers = new ArrayList<>();

    public ServerDashboard() {
    }

    public ServerDashboard(ManagedGuildMessage managedGuildMessage) {
        this.managedGuildMessage = managedGuildMessage;
    }

    public String getName() {
        return managedGuildMessage.getName();
    }
}
