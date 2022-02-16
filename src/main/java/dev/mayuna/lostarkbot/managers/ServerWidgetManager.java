package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import dev.mayuna.lostarkbot.Main;
import dev.mayuna.lostarkbot.objects.ServerWidget;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerWidgetManager {

    public static final String DATA_FILE = "./server_widgets.json";

    private final static @Getter List<ServerWidget> widgets = new ArrayList<>();

    /**
     * Gets {@link ServerWidget} from loaded widgets
     * @param channel Channel
     * @return Null if {@link ServerWidget} does not exists in specified channel
     */
    public static ServerWidget getServerWidgetByChannel(AbstractChannel channel) {
        synchronized (widgets) {
            for (ServerWidget serverWidget : widgets) {
                if (serverWidget.getManagedMessage().getMessageChannel().getIdLong() == channel.getIdLong()) {
                    return serverWidget;
                }
            }

            return null;
        }
    }

    /**
     * Determines if {@link ServerWidget} exists in specified channel
     * @param channel Channel
     * @return true if is, false otherwise
     */
    public static boolean isServerWidgetInChannel(AbstractChannel channel) {
        return getServerWidgetByChannel(channel) != null;
    }

    /**
     * Tries to create {@link ServerWidget} in specified channel
     * @param textChannel Channel
     * @return Null if there already is some {@link ServerWidget} in specified server or if bot was unable to create/edit message
     */
    public static ServerWidget createServerWidget(TextChannel textChannel) {
        if (isServerWidgetInChannel(textChannel)) {
            return null;
        }

        ManagedMessage managedMessage = new ManagedMessage(UUID.randomUUID().toString(), textChannel.getGuild(), textChannel);
        ServerWidget serverWidget = new ServerWidget(managedMessage);

        if (update(serverWidget)) {
            widgets.add(serverWidget);
            return serverWidget;
        }

        return null;
    }

    /**
     * Tries to delete {@link ServerWidget} from specified channel
     * @param channel Channel
     * @return True if {@link ServerWidget} was successfully removed
     */
    public static boolean deleteServerWidget(AbstractChannel channel) {
        ServerWidget serverWidget = getServerWidgetByChannel(channel);

        if (serverWidget == null) {
            return false;
        }

        synchronized (widgets) {
            widgets.remove(serverWidget);
        }

        ManagedMessage managedMessage = serverWidget.getManagedMessage();
        managedMessage.updateEntries(Main.getJda());

        try {
            managedMessage.getMessage().delete().complete();
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Widget " + serverWidget.getName() + "! Probably user deleted guild/channel or bot does not have permissions. However, it was removed from internal cache.");
            return false;
        }

        return true;
    }

    /**
     * Updates message in {@link ServerWidget}
     * @param serverWidget {@link ServerWidget}
     * @return True if message was successfully sent/edited
     */
    public static boolean update(ServerWidget serverWidget) {
        ManagedMessage managedMessage = serverWidget.getManagedMessage();

        try {
            managedMessage.updateEntries(Main.getJda());
            managedMessage.sendOrEditMessage(new MessageBuilder().setEmbeds(DiscordUtils.getDefaultEmbed().setDescription("Update: `" + serverWidget.getName() + "`").build()));
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.warn("Failed to update Server Widget " + serverWidget.getName() + "! Probably user deleted guild/channel or bot does not have permissions.");
            return false;
        }
    }

    public static void load() {
        Logger.info("Loading Server Widgets...");
        widgets.clear();

        int index = 0;
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(DATA_FILE);
            JsonArray jsonArray = mayuJson.getOrCreate("serverWidgets", new JsonArray()).getAsJsonArray();
            Gson gson = Utils.createGson();

            for (JsonElement jsonElement : jsonArray) {
                try {
                    widgets.add(gson.fromJson(jsonElement, ServerWidget.class));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Logger.warn("Unable to load Server Widget with index " + index + "!");
                }

                index++;
            }

            Logger.success("Successfully loaded " + widgets.size() + " Server Widgets!" );
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Failed to load Server Widgets!");
        }
    }

    public static void save() {
        Logger.info("Saving Server Widgets...");

        Logger.success("Successfully saved Server Widgets!");
    }
}
