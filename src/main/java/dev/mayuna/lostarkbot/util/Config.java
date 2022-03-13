package dev.mayuna.lostarkbot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.mayuslibrary.logging.LogPrefix;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private static @Getter @Setter String prefix = "la!";
    private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    private static @Getter @Setter String logLevel = "debug";
    private static @Getter @Setter long exceptionMessageChannelID = 0;
    private static @Getter @Setter long ownerID = 0;
    private static @Getter @Setter boolean debug = false;

    private static @Getter List<String> contributors = new ArrayList<>();

    private static @Getter List<String> westNorthAmerica = new ArrayList<>();
    private static @Getter List<String> eastNorthAmerica = new ArrayList<>();
    private static @Getter List<String> centralEurope = new ArrayList<>();
    private static @Getter List<String> southAmerica = new ArrayList<>();
    private static @Getter List<String> europeWest = new ArrayList<>();

    public static boolean load() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            logLevel = mayuJson.getOrCreate("logLevel", new JsonPrimitive(logLevel)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();

            contributors = JsonUtils.toStringList(mayuJson.getOrCreate("contributors", new JsonArray()).getAsJsonArray());

            westNorthAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("westNorthAmerica", new JsonArray()).getAsJsonArray());
            eastNorthAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("eastNorthAmerica", new JsonArray()).getAsJsonArray());
            centralEurope = JsonUtils.toStringList(mayuJson.getOrCreate("centralEurope", new JsonArray()).getAsJsonArray());
            southAmerica = JsonUtils.toStringList(mayuJson.getOrCreate("southAmerica", new JsonArray()).getAsJsonArray());
            europeWest = JsonUtils.toStringList(mayuJson.getOrCreate("europeWest", new JsonArray()).getAsJsonArray());

            mayuJson.saveJson();

            Logger.setLevel(logLevel);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while loading config from path " + Constants.CONFIG_PATH + "!");
            return false;
        }
    }

    public static void save() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            mayuJson.add("prefix", prefix);
            mayuJson.add("token", token);
            mayuJson.add("logLevel", logLevel);
            mayuJson.add("exceptionMessageChannelID", exceptionMessageChannelID);
            mayuJson.add("ownerID", ownerID);
            mayuJson.add("debug", debug);

            mayuJson.add("contributors", JsonUtils.toStringJsonArray(contributors));

            mayuJson.add("westNorthAmerica", JsonUtils.toStringJsonArray(westNorthAmerica));
            mayuJson.add("eastNorthAmerica", JsonUtils.toStringJsonArray(eastNorthAmerica));
            mayuJson.add("centralEurope", JsonUtils.toStringJsonArray(centralEurope));
            mayuJson.add("southAmerica", JsonUtils.toStringJsonArray(southAmerica));
            mayuJson.add("europeWest", JsonUtils.toStringJsonArray(europeWest));

            mayuJson.saveJson();

            Logger.success("Successfully saved config!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while saving config to path " + Constants.CONFIG_PATH + "!");
        }
    }
}
