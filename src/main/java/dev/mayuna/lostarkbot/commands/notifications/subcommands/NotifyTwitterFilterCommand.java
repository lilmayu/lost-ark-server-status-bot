package dev.mayuna.lostarkbot.commands.notifications.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.NotificationChannelHelper;
import dev.mayuna.lostarkbot.objects.features.NotificationChannel;
import dev.mayuna.lostarkbot.util.AutoMessageUtils;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.mayusjdautils.util.MessageInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class NotifyTwitterFilterCommand extends SlashCommand {

    public NotifyTwitterFilterCommand() {
        this.name = "twitter-filter";
        this.help = "Allows you to set-up filter keywords for tweets";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getAddRemoveArgument());
        options.add(new OptionData(OptionType.STRING, "keyword", "Keyword to filter within tweets (non case sensitive)", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Utils.makeEphemeral(event, true);
        TextChannel textChannel = event.getTextChannel();
        InteractionHook interactionHook = event.getHook();

        OptionMapping actionOption = AutoMessageUtils.getOptionMapping(event, "action");
        OptionMapping keywordOption = AutoMessageUtils.getOptionMapping(event, "keyword");

        if (actionOption == null || keywordOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightNotificationChannel(textChannel, interactionHook)) {
            return;
        }

        NotificationChannel notificationChannel = NotificationChannelHelper.getNotificationChannel(textChannel);
        String keyword = keywordOption.getAsString();

        switch (actionOption.getAsString()) {
            case "add" -> {
                if (notificationChannel.getTwitterKeywords().size() == 30) {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Maximum number of filtering keywords is **30**.").build()).queue();
                    return;
                }

                if (notificationChannel.addToTwitterKeywords(keywordOption.getAsString())) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added keyword **" + keyword + "** into filter.").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Keyword **" + keyword + "** is already in filter!").build()).queue();
                }
            }
            case "remove" -> {
                if (notificationChannel.removeFromTwitterKeywords(keywordOption.getAsString())) {
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed keyword **" + keyword + "** from filter.").build()).queue();
                    notificationChannel.save();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Keyword **" + keyword + "** is not on the filter!").build()).queue();
                }
            }
        }
    }
}
