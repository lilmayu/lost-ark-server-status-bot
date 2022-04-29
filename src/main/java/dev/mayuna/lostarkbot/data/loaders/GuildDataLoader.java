package dev.mayuna.lostarkbot.data.loaders;

import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.features.GuildData;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import dev.mayuna.mayusjsonutils.JsonUtil;
import lombok.NonNull;

import java.io.File;

public class GuildDataLoader {

    /**
     * Loads {@link GuildData} from {@link File}
     *
     * @param file Non-null {@link File}
     *
     * @return Nullable {@link GuildData} (if it fails to load)
     */
    public static GuildData loadGuildDataFromFile(@NonNull File file) {
        try {
            if (!file.exists()) {
                return null;
            }
            return Utils.getGson().fromJson(JsonUtil.createOrLoadJsonFromFile(file).getJsonObject(), GuildData.class);
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("Exception occurred while loading GuildData from file '" + file.getName() + "'!");
            return null;
        }
    }

    public static boolean updateEntries(@NonNull GuildData guildData) {
        try {
            if (guildData.updateEntries(Main.getMayuShardManager().get())) {
                Logger.flow("[GUILD-LOAD] Successfully loaded GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")");
                return true;
            }
        } catch (InvalidGuildIDException exception) {
            Logger.warn("[GUILD-LOAD] Unable to update entries in GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")! (Bot is not in this guild anymore, won't be loaded into cache)");
        } catch (Exception exception) {
            Logger.throwing(exception);
            Logger.error("[GUILD-LOAD] Unable to update entries in GuildData " + guildData.getRawGuildID() + " (" + guildData.getName() + ")! (Unknown Exception, won't be loaded into cache)");
        }

        return false;
    }
}
