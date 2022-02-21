package dev.mayuna.lostarkbot.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.util.JsonUtils;
import dev.mayuna.mayusjdautils.managed.ManagedGuildMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ServerDashboard {

    private @Getter ManagedGuildMessage managedGuildMessage;

    private @Getter boolean showLegend = true;
    private @Getter List<String> hiddenRegions = new ArrayList<>();
    private @Getter List<String> favoriteServers = new ArrayList<>();

    public ServerDashboard() {

    }

    public ServerDashboard(ManagedGuildMessage managedGuildMessage) {
        this.managedGuildMessage = managedGuildMessage;
    }

    public String getName() {
        return managedGuildMessage.getName();
    }

    public static ServerDashboard fromJsonObject(JsonObject jsonObject) {
        ServerDashboard serverDashboard = new ServerDashboard();

        serverDashboard.managedGuildMessage = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(jsonObject.getAsJsonObject("managedMessage"), ManagedGuildMessage.class);
        serverDashboard.managedGuildMessage.updateEntries(Main.getJda());
        serverDashboard.showLegend = jsonObject.get("showLegend").getAsBoolean();
        serverDashboard.hiddenRegions = JsonUtils.toStringList(jsonObject.getAsJsonArray("hiddenRegions"));
        serverDashboard.favoriteServers = JsonUtils.toStringList(jsonObject.getAsJsonArray("favoriteServers"));

        return serverDashboard;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("managedMessage", new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJsonTree(managedGuildMessage));
        jsonObject.addProperty("showLegend", showLegend);
        jsonObject.add("hiddenRegions", JsonUtils.toStringJsonArray(hiddenRegions));
        jsonObject.add("favoriteServers", JsonUtils.toStringJsonArray(favoriteServers));

        return jsonObject;
    }
}
