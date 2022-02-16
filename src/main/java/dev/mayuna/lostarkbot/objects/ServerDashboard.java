package dev.mayuna.lostarkbot.objects;

import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.util.JsonUtils;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ServerDashboard {

    private @Getter ManagedMessage managedMessage;

    private @Getter boolean showLegend = true;
    private @Getter List<String> hiddenRegions = new ArrayList<>();
    private @Getter List<String> favoriteServers = new ArrayList<>();

    public ServerDashboard() {

    }

    public ServerDashboard(ManagedMessage managedMessage) {
        this.managedMessage = managedMessage;
    }

    public String getName() {
        return managedMessage.getName();
    }

    public static ServerDashboard fromJsonObject(JsonObject jsonObject) {
        ServerDashboard serverDashboard = new ServerDashboard();

        serverDashboard.managedMessage = new ManagedMessage(jsonObject.getAsJsonObject("managedMessage"));
        serverDashboard.showLegend = jsonObject.get("showLegend").getAsBoolean();
        serverDashboard.hiddenRegions = JsonUtils.toStringList(jsonObject.getAsJsonArray("hiddenRegions"));
        serverDashboard.favoriteServers = JsonUtils.toStringList(jsonObject.getAsJsonArray("favoriteServers"));

        return serverDashboard;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("managedMessage", managedMessage.toJsonObject());
        jsonObject.addProperty("showLegend", showLegend);
        jsonObject.add("hiddenRegions", JsonUtils.toStringJsonArray(hiddenRegions));
        jsonObject.add("favoriteServers", JsonUtils.toStringJsonArray(favoriteServers));

        return jsonObject;
    }
}
