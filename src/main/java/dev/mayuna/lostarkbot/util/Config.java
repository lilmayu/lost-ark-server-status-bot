package dev.mayuna.lostarkbot.util;

import com.google.gson.JsonPrimitive;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.mayuslibrary.logging.LogPrefix;
import lombok.Getter;
import lombok.Setter;

public class Config {

    private static @Getter @Setter String prefix = "la!";
    private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    private static @Getter @Setter long exceptionMessageChannelID = 0;
    private static @Getter @Setter long ownerID = 0;
    private static @Getter @Setter boolean debug = false;

    public static boolean load() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();

            mayuJson.saveJson();

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
            mayuJson.add("exceptionMessageChannelID", exceptionMessageChannelID);
            mayuJson.add("ownerID", ownerID);
            mayuJson.add("debug", debug);

            mayuJson.saveJson();

            Logger.success("Successfully saved config!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Error occurred while saving config to path " + Constants.CONFIG_PATH + "!");
        }
    }
}
