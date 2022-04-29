package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.util.logging.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class MessageSender {

    public static void sendMessage(Message message, TextChannel textChannel, UpdateType type) {
        try {
            textChannel.sendMessage(message).queue(success -> {
                Logger.flow("Successfully sent message (" + success.getId() + ") with type " + type + " into channel " + textChannel);
            }, failure -> {
                if (failure instanceof PermissionException || failure instanceof ErrorResponseException) {
                    Logger.get().flow(failure);
                    Logger.warn("Could not send message with type " + type + " into channel " + textChannel + " (Permission or Response Exception)");
                } else {
                    Logger.throwing(failure);

                    Logger.error("Could not send message with type " + type + " into channel " + textChannel + " (Unknown exception)");
                }
            });
            Utils.waitByConfigValue(type);
        } catch (PermissionException | ErrorResponseException exception) {
            Logger.get().flow(exception);
            Logger.warn("Could not send message with type " + type + " into channel " + textChannel + " (Permission or Response Exception)");
        } catch (Exception exception) {
            Logger.throwing(exception);

            Logger.error("Could not send message with type " + type + " into channel " + textChannel + " (Unknown exception)");
        }
    }
}
