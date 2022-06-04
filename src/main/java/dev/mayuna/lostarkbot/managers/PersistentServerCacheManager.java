package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.util.Constants;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.lostarkfetcher.objects.api.LostArkServer;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PersistentServerCacheManager {

    private static final @Getter List<LostArkServer> serverCache = Collections.synchronizedList(new LinkedList<>());

    /**
     * Loads servers saved in file
     *
     * @return True if loading was successful
     */
    public static boolean load() {
        MayuJson mayuJson = null;

        try {
            mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.PERSISTENT_SERVER_CACHE);
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("[PERSISTENT-SERVER-CACHE] Could not create " + Constants.PERSISTENT_SERVER_CACHE + " file!");
            return false;
        }

        JsonArray jsonArray = mayuJson.getOrCreate("serverCache", new JsonArray()).getAsJsonArray();

        for (JsonElement jsonElement : jsonArray) {
            try {
                LostArkServer lostArkServer = new Gson().fromJson(jsonElement, LostArkServer.class);
                serverCache.add(lostArkServer);
            } catch (Exception exception) {
                exception.printStackTrace();
                Logger.warn("[PERSISTENT-SERVER-CACHE] Could not load JSON Element: " + jsonElement);
            }
        }

        try {
            mayuJson.saveJson();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("[PERSISTENT-SERVER-CACHE] Could not save " + Constants.PERSISTENT_SERVER_CACHE + " file!");
            return false;
        }

        Logger.success("[PERSISTENT-SERVER-CACHE] Loaded " + serverCache.size() + " servers.");
        return true;
    }

    /**
     * Saves current cache into a file
     *
     * @return True if saving was successful
     */
    public static boolean save() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        synchronized (serverCache) {
            for (LostArkServer lostArkServer : serverCache) {
                jsonArray.add(new Gson().toJsonTree(lostArkServer));
            }
        }

        jsonObject.add("serverCache", jsonArray);

        try {
            JsonUtil.saveJson(jsonObject, new File(Constants.PERSISTENT_SERVER_CACHE));
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("[PERSISTENT-SERVER-CACHE] Failed to save cache into file " + Constants.PERSISTENT_SERVER_CACHE + "!");
            return false;
        }

        Logger.success("[PERSISTENT-SERVER-CACHE] Successfully saved " + jsonArray.size() + " servers.");
        return true;
    }

    /**
     * Updates the cache with new servers in list
     *
     * @param lostArkServers {@link LostArkServer} list
     */
    public static void updateServerCache(List<LostArkServer> lostArkServers) {
        for (LostArkServer lostArkServer : lostArkServers) {
            if (!doesServerExist(lostArkServer.getName())) {
                synchronized (serverCache) {
                    serverCache.add(lostArkServer);
                }
            }
        }

        save();
    }

    /**
     * Gets server by its name from cache. Search will be non-case sensitive.
     *
     * @param serverName Server name
     *
     * @return Nullable {@link LostArkServer}
     */
    public static @Nullable LostArkServer getServerByName(String serverName) {
        synchronized (serverCache) {
            for (LostArkServer lostArkServer : serverCache) {
                if (lostArkServer.is(serverName)) {
                    return lostArkServer;
                }
            }
        }

        return null;
    }

    /**
     * Tells if the specified server exists in cache. Basically calls {@link #getServerByName(String)} and checks if the return is non-null
     *
     * @param serverName Server name
     *
     * @return True if exists, false otherwise
     */
    public static boolean doesServerExist(String serverName) {
        return getServerByName(serverName) != null;
    }
}
