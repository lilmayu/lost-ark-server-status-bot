package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.objects.other.StatusWhitelistObject;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyStatusBlacklistCommand extends SlashCommand {

    public NotifyStatusBlacklistCommand() {
        this.name = "status-blacklist";
        this.help = "Allows you to blacklist specific statuses in current Notification Channel";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(3);
        options.add(Utils.getAddRemoveArgument());
        options.add(Utils.getChangedFromToStatusArgument());
        options.add(Utils.getStatusWithOfflineArgument());
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (!Utils.makeEphemeral(event, true)) {
            return;
        }
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        if (!AutoMessageUtils.isBotFullyLoaded(interactionHook)) {
            return;
        }

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping typeOption = AutoMessageUtils.getOptionMapping(event, "type");
        OptionMapping statusOption = AutoMessageUtils.getOptionMapping(event, "status");

        if (actionOption == null || typeOption == null || statusOption == null) {
            return;
        }

        StatusWhitelistObject.Type type = StatusWhitelistObject.Type.get(typeOption.getAsString());

        if (type == null) {
            AutoMessageUtils.sendInvalidArgumentMessage(interactionHook, "type");
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        String status = statusOption.getAsString();
        String typePretty = type == StatusWhitelistObject.Type.FROM ? "Changed from" : "Changed to";

        StatusWhitelistObject statusWhitelistObject = new StatusWhitelistObject(type, status);

        switch (actionOption.getAsString()) {
            case "add" -> {
                if (notificationChannel.addToBlacklist(statusWhitelistObject)) {
                    notificationChannel.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully blacklisted **" + typePretty + " " + status + "** status change!").build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Status change **" + typePretty + " " + status + "** is already blacklisted!").build()).queue();
                }
            }
            case "remove" -> {
                if (notificationChannel.removeFromBlacklist(statusWhitelistObject)) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed blacklisted **" + typePretty + " " + status + "** status change!").build())
                            .queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Status change **" + typePretty + " " + status + "** is not blacklisted!\n\nTIP: If you want to clear whole blacklist, you can use `/notify clear` command.")
                                                               .build()).queue();
                }
            }
        }
    }
}
