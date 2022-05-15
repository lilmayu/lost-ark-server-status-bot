package dev.mayuna.lostarkbot.commands.dashboard.subcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.mayuna.lostarkbot.helpers.ServerDashboardHelper;
import dev.mayuna.lostarkbot.objects.features.ServerDashboard;
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

public class DashboardFavoriteCommand extends SlashCommand {

    public DashboardFavoriteCommand() {
        this.name = "favorite";
        this.help = "Adds/removes specified server into/from Favorites section";

        List<OptionData> options = new ArrayList<>(2);
        options.add(Utils.getAddRemoveArgument());
        options.add(new OptionData(OptionType.STRING, "server", "Server name, ex. Calvasus", true));
        this.options = options;

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
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
        OptionMapping serverOption = AutoMessageUtils.getOptionMapping(event, "server");

        if (actionOption == null || serverOption == null) {
            return;
        }

        if (!AutoMessageUtils.isEverythingAlrightDashboard(textChannel, interactionHook)) {
            return;
        }

        String serverName = serverOption.getAsString();
        ServerDashboard dashboard = ServerDashboardHelper.getServerDashboard(textChannel);

        switch (actionOption.getAsString()) {
            case "add" -> {
                if (dashboard.addToFavorites(serverName)) {
                    dashboard.update();
                    dashboard.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully added server `" + Utils.getCorrectServerName(serverName) + "` into Favorite section!")
                                                               .build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` is already added in Favorite section or this server does not exist!")
                                                               .build()).queue();
                }
            }

            case "remove" -> {
                if (dashboard.removeFromFavorites(serverName)) {
                    dashboard.update();
                    dashboard.save();
                    interactionHook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed server `" + serverName + "` from Favorite section!").build()).queue();
                } else {
                    interactionHook.editOriginalEmbeds(MessageInfo.errorEmbed("Server with name `" + serverOption.getAsString() + "` is not in Favorite section.").build()).queue();
                }
            }
        }
    }
}
