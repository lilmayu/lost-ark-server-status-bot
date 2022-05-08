package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyTwitterPingCommand extends SlashCommand {

    public NotifyTwitterPingCommand() {
        this.name = "twitter-ping";
        this.help = "Allows you to add/remove roles which will be pinged on new Tweets";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getAddRemoveArgument());
        options.add(new OptionData(OptionType.ROLE, "role", "Role to ping on new Tweets", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping roleOption = AutoMessageUtils.getOptionMapping(event, "role");

        if (actionOption == null || roleOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        Role role = roleOption.getAsRole();

        switch (actionOption.getAsString()) {
            case "add" -> {
                if (notificationChannel.addToTwitterPingRoleIds(role)) {
                    notificationChannel.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added role " + role.getAsMention() + " to roles, which will be pinged on new Tweets!")
                                                    .build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Role " + role.getAsMention() + " is already added!").build()).queue();
                }
            }
            case "remove" -> {
                if (notificationChannel.removeFromTwitterPingRoleIds(role)) {
                    notificationChannel.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed role " + role.getAsMention() + " from roles, which will be pinged on new Tweets!")
                                                    .build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Cannot remove role " + role.getAsMention() + " since it was not added!").build()).queue();
                }
            }
        }
    }
}
